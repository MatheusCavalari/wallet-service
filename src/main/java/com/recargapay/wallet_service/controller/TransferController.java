package com.recargapay.wallet_service.controller;

import com.recargapay.wallet_service.dto.TransferRequest;
import com.recargapay.wallet_service.dto.TransferResponse;
import com.recargapay.wallet_service.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        var result = transferService.transfer(
                request.fromWalletId(),
                request.toWalletId(),
                request.amount(),
                idempotencyKey
        );

        return new TransferResponse(
                result.transferId(),
                request.fromWalletId(),
                request.toWalletId(),
                request.amount(),
                result.fromBalance(),
                result.toBalance(),
                result.occurredAt()
        );
    }
}
