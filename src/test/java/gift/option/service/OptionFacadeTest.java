package gift.option.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.product.service.ProductService;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("OptionFacade")
@ExtendWith(MockitoExtension.class)
class OptionFacadeTest {

    @Mock
    private OptionService optionService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OptionFacade optionFacade;

    private OptionResponse optionResponse() {
        return new OptionResponse(1L, "옵션A", 100);
    }

    private OptionRequest optionRequest() {
        return new OptionRequest("옵션A", 100);
    }

    @Nested
    @DisplayName("옵션 목록을 조회할 때,")
    class GetOptions {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("OptionService에 위임하여 목록을 반환한다.")
            void delegatesToOptionService() {
                // given
                final OptionResponse response = optionResponse();
                given(optionService.getOptions(1L)).willReturn(List.of(response));

                // when
                final List<OptionResponse> result = optionFacade.getOptions(1L);

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).id()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                willThrow(new NoSuchElementException("상품을 찾을 수 없습니다."))
                    .given(productService).findProduct(99L);

                // when & then
                assertThatThrownBy(() -> optionFacade.getOptions(99L))
                    .isInstanceOf(NoSuchElementException.class);
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
            @DisplayName("OptionService에 위임하여 생성된 옵션을 반환한다.")
            void delegatesToOptionService() {
                // given
                final OptionRequest request = optionRequest();
                final OptionResponse response = optionResponse();
                given(optionService.createOption(1L, request)).willReturn(response);

                // when
                final OptionResponse result = optionFacade.createOption(1L, request);

                // then
                assertThat(result.id()).isEqualTo(1L);
                assertThat(result.name()).isEqualTo("옵션A");
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                final OptionRequest request = optionRequest();
                given(optionService.createOption(99L, request))
                    .willThrow(new NoSuchElementException("상품을 찾을 수 없습니다."));

                // when & then
                assertThatThrownBy(() -> optionFacade.createOption(99L, request))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("중복된 옵션명이면 예외가 발생한다.")
            void throwsWhenDuplicateName() {
                // given
                final OptionRequest request = optionRequest();
                given(optionService.createOption(1L, request))
                    .willThrow(new IllegalArgumentException("이미 존재하는 옵션명입니다."));

                // when & then
                assertThatThrownBy(() -> optionFacade.createOption(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 옵션명입니다.");
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
            @DisplayName("OptionService에 위임하여 삭제한다.")
            void delegatesToOptionService() {
                // given
                willDoNothing().given(optionService).deleteOption(1L, 1L);

                // when
                optionFacade.deleteOption(1L, 1L);

                // then
                verify(optionService).deleteOption(1L, 1L);
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("상품이 없으면 예외가 발생한다.")
            void throwsWhenProductNotFound() {
                // given
                willThrow(new NoSuchElementException("상품을 찾을 수 없습니다."))
                    .given(productService).findProduct(99L);

                // when & then
                assertThatThrownBy(() -> optionFacade.deleteOption(99L, 1L))
                    .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("옵션이 1개뿐이면 예외가 발생한다.")
            void throwsWhenLastOption() {
                // given
                willThrow(new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다."))
                    .given(optionService).deleteOption(1L, 1L);

                // when & then
                assertThatThrownBy(() -> optionFacade.deleteOption(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
            }
        }
    }
}
