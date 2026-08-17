package com.nhnacademy.inventory.global.cient;

import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

// account 연동 확인 후 수정
@Component
public class AccountClient {
    private static final String ACCOUNT_SERVER = "/api/accounts/internal";

    private final RestClient restClient;

    public AccountClient(@Qualifier("accountRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<AccountResponse> searchByEmail(String email) {
        return restClient.get()
                .uri(UriComponentsBuilder
                        .fromPath(ACCOUNT_SERVER + "/search")
                        .queryParam("email", email)
                        .toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
    }

    public List<AccountResponse> findByUuids(List<UUID> accountUuids) {
        return restClient.get()
                .uri(UriComponentsBuilder
                        .fromPath(ACCOUNT_SERVER)
                        .queryParam("uuids", accountUuids)
                        .toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
    }

    public AccountResponse deleteAccount(UUID accountUuid) {
        return restClient.delete()
                .uri(UriComponentsBuilder
                        .fromPath(ACCOUNT_SERVER)
                        .queryParam("account-uuid", accountUuid)
                        .toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<AccountResponse>() {});
    }

    public void deleteAccounts(List<UUID> accountUuids) {
        restClient.post()
                .uri(ACCOUNT_SERVER + "/bulk-delete")
                .body(accountUuids)
                .retrieve()
                .toBodilessEntity();
    }
}
