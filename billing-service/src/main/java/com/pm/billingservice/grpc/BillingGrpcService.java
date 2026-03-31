package com.pm.billingservice.grpc;

import billing.*;
import com.google.api.Billing;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver){
        log.info("createBillingAccount request received {} ",billingRequest.toString());
        // Business logic - eg save the database, calulation etc.

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("Active")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void numberToCharacter(NumberRequest request,
                                  StreamObserver<CharacterResponse> responseObserver){
        int number = request.getNumber();

        if (number < 1 || number > 26) {
            responseObserver.onError(new IllegalArgumentException("Number must be 1-26"));
            return;
        }

        char character = (char) ('A' + number - 1);

        CharacterResponse response = CharacterResponse.newBuilder()
                .setCharacter(String.valueOf(character))
                .setMessage("Number " + number + " corresponds to " + character)
                .build();
        log.info("numberToCharacter request received {} ",request.toString());
        log.info("Response: " + response.toString());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
