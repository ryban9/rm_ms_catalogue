package ec.rmdetalles.catalogo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StorageService {

    private final String supabaseUrl;
    private final RestClient restClient;
    private final String secretKey;
    private final String bucket;

    public StorageService(
            @Value("${app.supabase.url}") String supabaseUrl,
            @Value("${app.supabase.secret-key}") String secretKey,
            @Value("${app.supabase.storage-bucket}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl.replaceAll("/+$", "");
        this.restClient = RestClient.builder()
                .baseUrl(this.supabaseUrl + "/storage/v1")
                .build();
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    public void subir(String ruta, byte[] contenido, String mimeType) {
        restClient.post()
                .uri("/object/{bucket}/{ruta}", bucket, ruta)
                .header("apikey", secretKey)
                .header("x-upsert", "false")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(contenido)
                .retrieve()
                .toBodilessEntity();
    }

    public String urlPublica(String ruta) {
        return supabaseUrl + "/storage/v1/object/public/"
                + bucket + "/"
                + ruta.replace(" ", "%20");
    }
}
