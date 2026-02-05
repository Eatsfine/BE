package com.eatsfine.eatsfine.domain.store.validator;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreValidator {
    private final StoreRepository storeRepository;

    public void validateStoreOwner(Long storeId, String email) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        if(!store.getOwner().getEmail().equals(email)) {
            throw new StoreException(StoreErrorStatus._NOT_STORE_OWNER);
        }

    }
}
