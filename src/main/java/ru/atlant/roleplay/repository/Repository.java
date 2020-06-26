package ru.atlant.roleplay.repository;

import java.util.concurrent.CompletableFuture;

public interface Repository<T> {

	CompletableFuture<String> fetch();

}
