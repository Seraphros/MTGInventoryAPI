package fr.cszw.mtginventoryapi.Services;


import org.springframework.context.annotation.Scope;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Scope("singleton")
public class FileDownloader {

    public FileDownloader() {

    }

    public WebClient webClientWithLargeBuffer() {
        return WebClient.builder()
                .baseUrl("https://data.scryfall.io")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs()
                                        .maxInMemorySize(2 * 1024)
                        )
                        .build())
                .build();
    }
    public void download(String URL_LOCATION) {
        File dstFile;
// check the directory for existence.
        String dstFolder = System.getenv("PRICE_FILE");

        try {
            Files.createDirectories(Paths.get(new File(dstFolder).getParentFile().getAbsolutePath()));
            dstFile = new File(dstFolder);
            if (dstFile.exists()) dstFile.delete();

            Flux<DataBuffer> dataBuffer = webClientWithLargeBuffer()
                    .get()
                    .uri(URL_LOCATION)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);

            DataBufferUtils.write(dataBuffer, dstFile.toPath(),
                            StandardOpenOption.CREATE)
                    .share().block();

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
