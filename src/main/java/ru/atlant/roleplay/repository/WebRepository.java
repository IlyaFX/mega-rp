package ru.atlant.roleplay.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import ru.atlant.roleplay.util.ExecutorUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
@Getter
public abstract class WebRepository<T> implements Repository<T> {

	private final String url;

	@Override
	public CompletableFuture<String> connect() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Jsoup.connect(url).ignoreContentType(true).execute().body();
			} catch (Exception ex) {
				throw new CompletionException(ex);
			}
		}, ExecutorUtils.EXECUTOR);
	}
}