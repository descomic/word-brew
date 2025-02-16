/*************************************************************************
 * ULLINK CONFIDENTIAL INFORMATION
 * _______________________________
 *
 * All Rights Reserved.
 *
 * NOTICE: This file and its content are the property of Ullink. The
 * information included has been classified as Confidential and may
 * not be copied, modified, distributed, or otherwise disseminated, in
 * whole or part, without the express written permission of Ullink.
 ************************************************************************/
package org.descomic.gameservice.services;

import org.descomic.gameservice.model.Guess;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class GuessStorageService {

    private final Set<Guess> store = new HashSet<>();

    public void storeGuess(Guess guess) {
        store.add(guess);
    }

    public Collection<Guess> getStore() {
        return store.stream().toList();
    }
}
