package it.sdcc.projectsonlinebackend.configurations;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class AzureBlobStorageConfiguration {

    @Value("${spring.azure.storage.default-endpoints-protocol}")
    private String DEFAULT_ENDPOINTS_PROTOCOL;

    @Value("${spring.azure.storage.account-name}")
    private String AZURE_ACCOUNT_NAME;

    @Value("${spring.azure.storage.account-key}")
    private String AZURE_ACCOUNT_KEY;

    @Value("${spring.azure.storage.blob-endpoint}")
    private String AZURE_BLOB_ENDPOINT;

    /**Il seguente bean costruisce un client per accedere all'account di archiviazione ed effettuare delle operazioni*/
    @Bean
    public BlobServiceClient blobServiceClient() {
        String connectionString =
                String.format("DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s;BlobEndpoint=%s://%s/%s;",
                        DEFAULT_ENDPOINTS_PROTOCOL, AZURE_ACCOUNT_NAME, AZURE_ACCOUNT_KEY,
                        DEFAULT_ENDPOINTS_PROTOCOL, AZURE_BLOB_ENDPOINT, AZURE_ACCOUNT_NAME);
        return new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }//blobServiceClient
}//AzureBlobStorageConfiguration

