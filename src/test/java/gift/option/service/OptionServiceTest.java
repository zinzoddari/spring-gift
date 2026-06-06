package gift.option.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import gift.option.domain.Option;
import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OptionService optionService;

    private Option optionWithResponse() {
        final Option option = mock(Option.class);
        given(option.getId()).willReturn(1L);
        given(option.getName()).willReturn("옵션A");
        given(option.getQuantity()).willReturn(10);
        return option;
    }

    @Nested
    @DisplayName("옵션 목록을 조회할 때,")
    class GetOptions {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("상품의 옵션 목록을 반환한다.")
            void returnsOptionList() {
                // given
                final Option option = optionWithResponse();
                given(optionRepository.findByProductId(1L)).willReturn(List.of(option));

                // when
                final List<OptionResponse> result = optionService.getOptions(1L);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result).hasSize(1);
                    softly.assertThat(result.get(0).name()).isEqualTo("옵션A");
                    softly.assertThat(result.get(0).quantity()).isEqualTo(10);
                });
            }
        }
    }

    @Nested
    @DisplayName("옵션을 생성할 때,")
    class CreateOption {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("생성된 옵션을 반환한다.")
            void returnsCreatedOption() {
                // given
                final Option option = optionWithResponse();
                final OptionRequest request = new OptionRequest("옵션A", 10);
                given(productRepository.findById(1L)).willReturn(Optional.of(mock(Product.class)));
                given(optionRepository.existsByProductIdAndName(1L, "옵션A")).willReturn(false);
                given(optionRepository.save(any(Option.class))).willReturn(option);

                // when
                final OptionResponse result = optionService.createOption(1L, request);

                // then
                assertSoftly(softly -> {
                    softly.assertThat(result.name()).isEqualTo("옵션A");
                    softly.assertThat(result.quantity()).isEqualTo(10);
                });
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("허용되지 않는 특수문자가 포함된 이름이면 예외가 발생한다.")
            void throwsWhenInvalidName() {
                // given
                final OptionRequest request = new OptionRequest("옵션@이름", 10);

                // when & then
                assertThatThrownBy(() -> optionService.createOption(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                final OptionRequest request = new OptionRequest("옵션A", 10);
                given(productRepository.findById(1L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> optionService.createOption(1L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("이미 존재하는 옵션명이면 예외가 발생한다.")
            void throwsWhenDuplicateName() {
                // given
                final OptionRequest request = new OptionRequest("옵션A", 10);
                given(productRepository.findById(1L)).willReturn(Optional.of(mock(Product.class)));
                given(optionRepository.existsByProductIdAndName(1L, "옵션A")).willReturn(true);

                // when & then
                assertThatThrownBy(() -> optionService.createOption(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("재고를 차감할 때,")
    class SubtractQuantity {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("차감 후 옵션을 반환한다.")
            void returnsOptionAfterSubtract() {
                // given
                final Option option = mock(Option.class);
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));

                // when
                final Option result = optionService.subtractQuantity(1L, 3);

                // then
                assertThat(result).isEqualTo(option);
                verify(option).subtractQuantity(3);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("옵션이 없으면 예외가 발생한다.")
            void throwsWhenOptionNotFound() {
                // given
                given(optionRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> optionService.subtractQuantity(99L, 1))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("재고가 부족하면 예외가 발생한다.")
            void throwsWhenInsufficientStock() {
                // given
                final Option option = mock(Option.class);
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                willThrow(new IllegalArgumentException("차감할 수량이 현재 재고보다 많습니다."))
                    .given(option).subtractQuantity(100);

                // when & then
                assertThatThrownBy(() -> optionService.subtractQuantity(1L, 100))
                    .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("옵션을 삭제할 때,")
    class DeleteOption {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("삭제를 호출한다.")
            void callsDelete() {
                // given
                final Option option = mock(Option.class);
                final Option otherOption = mock(Option.class);
                given(optionRepository.findByProductId(1L)).willReturn(List.of(option, otherOption));
                given(optionRepository.findById(1L)).willReturn(Optional.of(option));
                willDoNothing().given(optionRepository).delete(option);

                // when
                optionService.deleteOption(1L, 1L);

                // then
                verify(optionRepository).delete(option);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("옵션이 1개뿐이면 예외가 발생한다.")
            void throwsWhenLastOption() {
                // given
                given(optionRepository.findByProductId(1L)).willReturn(List.of(mock(Option.class)));

                // when & then
                assertThatThrownBy(() -> optionService.deleteOption(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("옵션이 없으면 예외가 발생한다.")
            void throwsWhenOptionNotFound() {
                // given
                given(optionRepository.findByProductId(1L))
                    .willReturn(List.of(mock(Option.class), mock(Option.class)));
                given(optionRepository.findById(99L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> optionService.deleteOption(1L, 99L))
                    .isInstanceOf(NoSuchElementException.class);
            }
        }
    }
}
