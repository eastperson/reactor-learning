package com.chapter1_2.ep.security;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@SpringBootTest()
@EnableHypermediaSupport(type = HAL)
@AutoConfigureWebTestClient
public class SecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

//    @Autowired HypermediaWebTestClientConfigurer webClientConfigurer;

//    @BeforeEach
//    void setUp() {
//        this.webTestClient = this.webTestClient.mutateWith(webClientConfigurer);
//    }

    @Test
    @WithMockUser(username = "alice",roles = {"SOME_OTHER_ROLE"})
    void addingInventoryWithoutProperRoleFails(){
        this.webTestClient.post().uri("/")
                .exchange()
                // fordden은 인증(authenticated)는 됐지만 인가(authorized)는 받지 못함을 의미
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "bob",roles = {"INVENTORY"})
     void addingInventoryWithProperRoleSucceeds() {
        this.webTestClient
                .post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"iPhone 11\", \"description\":\"upgrade\",\"price\":999.99}")
                .exchange()
                .expectStatus().isCreated();

        this.itemRepository.findByName("iPhone 11")
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "alice", roles = {"SOME_OTHER_ROLE"})
    void addingItemWithoutProperRoleFails(){
        this.webTestClient
                .post().uri("/item/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"iPhone 11\", \"description\":\"upgrade\",\"price\":999.99}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "bob",roles = {"INVENTORY"})
    void addingItemWithoutProperRoleSucceeds(){
        this.webTestClient
                .post().uri("/item/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"iPhone X\", \"description\":\"upgrade\",\"price\":999.99}")
                .exchange()
                .expectStatus().isCreated();

        this.itemRepository.findByName("iPhone X")
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "alice",roles = {"INVENTORY"})
    void navigateToItemWithInventoryAuthority(){
        // api에 GET 요청
        RepresentationModel<?> root = this.webTestClient.get().uri("/item")
                .exchange()
                .expectBody(RepresentationModel.class)
                .returnResult().getResponseBody();

        CollectionModel<EntityModel<Item>> items = this.webTestClient.get()
                .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri())
                .exchange()
                .expectBody((new TypeReferences.CollectionModelType<EntityModel<Item>>(){}))
                .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(2);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(items.hasLink("add")).isTrue();

        // 첫 번째 Item의 EntityModel 획득
        EntityModel<Item> first = items.getContent().iterator().next();

        // 첫 번째 Item의 EntityModel에서 SELF 링크를 통해 첫 번째 Item 정보 획득
        EntityModel<Item> item = this.webTestClient.get()
                .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .exchange()
                .expectBody(new TypeReferences.EntityModelType<Item>())
                .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(3);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
        assertThat(item.hasLink("delete")).isTrue();

    }

}
