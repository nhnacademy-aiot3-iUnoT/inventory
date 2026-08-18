package com.nhnacademy.inventory.global.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhnacademy.inventory.global.error.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountApiClient {
    @Qualifier("accountRestClient")
    private final RestClient restClient;

    public <T> T get(String path, TypeReference<T> dataType) {
        return execute(() -> restClient.get()
                .uri(path)
                .retrieve()
                .body(responseTypeOf(dataType)));
    }

    public <T> T delete(String path, TypeReference<T> dataType) {
        return execute(() -> restClient.delete()
                .uri(path)
                .retrieve()
                .body(responseTypeOf(dataType)));
    }

    public void post(String path, Object body) {
        execute(() -> {
            restClient.post()
                .uri(path)
                .body(body)
                .retrieve()
                .toBodilessEntity();
            return null;
        });
    }

    private <T> T execute(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            throw convertException(e);
        }
    }

    private UpstreamServiceException convertException(RestClientException exception) {
        if (exception instanceof HttpStatusCodeException httpException) {
            log.warn(
                    "계정 서비스가 오류 응답을 반환했습니다. 상태 코드={}",
                    httpException.getStatusCode(),
                    httpException
            );
        } else if (exception instanceof ResourceAccessException) {
            log.warn("계정 서비스에 연결할 수 없습니다.", exception);
        } else {
            log.warn("계정 서비스 호출에 실패했습니다.", exception);
        }

        return new UpstreamServiceException(exception);
    }

    private <T> ParameterizedTypeReference<T> responseTypeOf(TypeReference<T> dataType) {
        return ParameterizedTypeReference.forType(dataType.getType());
    }
}
