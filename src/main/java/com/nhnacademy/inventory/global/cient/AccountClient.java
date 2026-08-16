package com.nhnacademy.inventory.global.cient;

import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountClient {
    private final RestClient restClient;
    private final String ACCOUNT_SERVER = "/api/core/internal/accounts";

    public List<AccountResponse> searchByEmail(String email) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ACCOUNT_SERVER + "/search")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
    }

    public List<AccountResponse> findByUuids(List<UUID> accountUuids) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ACCOUNT_SERVER)
                        .queryParam("uuids", accountUuids)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
    }

    public AccountResponse deleteAccount(UUID accountUuid) {
        return restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ACCOUNT_SERVER)
                        .queryParam("account-uuid", accountUuid)
                        .build())
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
