package gift.wish.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.auth.AuthenticationResolver;
import gift.member.domain.Member;
import gift.common.dto.PageResponse;
import gift.wish.dto.WishAddResult;
import gift.wish.dto.WishResponse;
import gift.wish.service.WishService;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("WishController")
@WebMvcTest(WishController.class)
class WishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishService wishService;

    @MockitoBean
    private AuthenticationResolver authenticationResolver;

    private Member member() {
        final Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        return member;
    }

    private WishResponse wishResponse() {
        return new WishResponse(1L, 1L, "상품A", 10_000, "http://img.jpg");
    }

    @Nested
    @DisplayName("위시리스트를 조회할 때,")
    class GetWishes {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("찜 목록을 페이지로 반환한다.")
            void returnsWishPage() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                given(wishService.getWishes(eq(1L), any(Pageable.class)))
                    .willReturn(PageResponse.from(new PageImpl<>(List.of(wishResponse()))));

                // when & then
                mockMvc.perform(get("/api/wishes")
                        .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("상품A"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("인증 실패 시 401을 반환한다.")
            void returnsUnauthorized() throws Exception {
                // given
                given(authenticationResolver.extractMember(any())).willReturn(null);

                // when & then
                mockMvc.perform(get("/api/wishes")
                        .header("Authorization", "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("위시리스트를 저장할 때,")
    class AddWish {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("새 찜이면 201을 반환한다.")
            void returnsCreated() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                given(wishService.addWish(eq(1L), any()))
                    .willReturn(new WishAddResult(wishResponse(), true));

                // when & then
                mockMvc.perform(post("/api/wishes")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"productId": 1}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("상품A"));
            }

            @Test
            @DisplayName("이미 찜한 상품이면 200을 반환한다.")
            void returnsOkWhenDuplicate() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                given(wishService.addWish(eq(1L), any()))
                    .willReturn(new WishAddResult(wishResponse(), false));

                // when & then
                mockMvc.perform(post("/api/wishes")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"productId": 1}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("상품A"));
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("인증 실패 시 401을 반환한다.")
            void returnsUnauthorized() throws Exception {
                // given
                given(authenticationResolver.extractMember(any())).willReturn(null);

                // when & then
                mockMvc.perform(post("/api/wishes")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"productId": 1}
                            """))
                    .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("상품이 없으면 404를 반환한다.")
            void returnsNotFound() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                given(wishService.addWish(eq(1L), any())).willThrow(new NoSuchElementException());

                // when & then
                mockMvc.perform(post("/api/wishes")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"productId": 99}
                            """))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("위시리스트를 삭제할 때,")
    class RemoveWish {

        @Nested
        @DisplayName("성공하면,")
        class WhenSuccess {

            @Test
            @DisplayName("204를 반환한다.")
            void returnsNoContent() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                willDoNothing().given(wishService).removeWish(eq(1L), eq(1L));

                // when & then
                mockMvc.perform(delete("/api/wishes/1")
                        .header("Authorization", "Bearer token"))
                    .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패하면,")
        class WhenFailed {

            @Test
            @DisplayName("인증 실패 시 401을 반환한다.")
            void returnsUnauthorized() throws Exception {
                // given
                given(authenticationResolver.extractMember(any())).willReturn(null);

                // when & then
                mockMvc.perform(delete("/api/wishes/1")
                        .header("Authorization", "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("찜이 없으면 404를 반환한다.")
            void returnsNotFound() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                willThrow(new NoSuchElementException()).given(wishService).removeWish(eq(1L), eq(99L));

                // when & then
                mockMvc.perform(delete("/api/wishes/99")
                        .header("Authorization", "Bearer token"))
                    .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("본인의 찜이 아니면 403을 반환한다.")
            void returnsForbidden() throws Exception {
                // given
                final Member member = member();
                given(authenticationResolver.extractMember(any())).willReturn(member);
                willThrow(new SecurityException()).given(wishService).removeWish(eq(1L), eq(1L));

                // when & then
                mockMvc.perform(delete("/api/wishes/1")
                        .header("Authorization", "Bearer token"))
                    .andExpect(status().isForbidden());
            }
        }
    }
}
