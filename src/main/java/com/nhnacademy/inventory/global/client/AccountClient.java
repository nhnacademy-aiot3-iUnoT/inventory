package com.nhnacademy.inventory.global.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountClient {
    private static final String ACCOUNT_SERVER = "/api/accounts/internal";
    private final AccountApiClient accountApiClient;

    public List<AccountResponse> searchByEmail(String email) {
        String path = UriComponentsBuilder
                .fromPath(ACCOUNT_SERVER + "/search")
                .queryParam("email", email)
                .toUriString();

        return accountApiClient.get(path, new TypeReference<>() {});
    }

    public List<AccountResponse> findByUuids(List<UUID> accountUuids) {
        String path = UriComponentsBuilder
                .fromPath(ACCOUNT_SERVER)
                .queryParam("uuids", accountUuids)
                .toUriString();

        return accountApiClient.get(path, new TypeReference<>() {});
    }

    public AccountResponse deleteAccount(UUID accountUuid) {
        String path = UriComponentsBuilder
                .fromPath(ACCOUNT_SERVER)
                .queryParam("account-uuid", accountUuid)
                .toUriString();

        return accountApiClient.delete(path, new TypeReference<>() {});
    }

    public void deleteAccounts(List<UUID> accountUuids) {
        accountApiClient.post(ACCOUNT_SERVER + "/bulk-delete", accountUuids);
    }
}
