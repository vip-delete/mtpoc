package com.example.web;

import com.example.dao.DataStore;
import com.example.model.Account;
import com.example.view.AccountResponse;
import com.example.view.BaseResponse;
import com.example.view.MoneyRequest;
import com.example.view.TransferRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("api/v1")
public class AccountControllerV1 {
    @Inject
    private DataStore ds;

    @POST
    @Path("accounts")
    public AccountResponse create() {
        long id = ds.createAccount();
        AccountResponse response = new AccountResponse();
        response.setId(id);
        response.setBalance("0");
        return response;
    }

    @GET
    @Path("accounts/{id}")
    public AccountResponse get(@PathParam("id") long id) {
        Account account = getAccount(id);
        return createAccountResponse(account);
    }

    @POST
    @Path("accounts/{id}/money")
    public BaseResponse money(@PathParam("id") long id, MoneyRequest request) {
        BigDecimal amount = getAmount(request.getAmount());
        if (!ds.update(id, amount)) {
            return createErrorResponse("Can't add/withdraw '" + request.getAmount() + "' to/from '" + id + "' account");
        }
        return createAccountResponse(getAccount(id));
    }

    @POST
    @Path("transfers")
    public BaseResponse transfer(TransferRequest request) {
        BigDecimal amount = getAmount(request.getAmount());
        if (!ds.transfer(request.getFromAccountId(), request.getToAccountId(), amount)) {
            return createErrorResponse("Can't transfer '" + request.getAmount() + "' from '" + request.getFromAccountId() + "' to '" + request.getToAccountId() + "'");
        }
        return new BaseResponse();
    }

    // private

    private Account getAccount(long id) {
        Account account = ds.getAccount(id);
        if (account == null) {
            throw new WebApplicationException(Response.ok(createErrorResponse("Account '" + id + "' not found")).build());
        }
        return account;
    }

    private BigDecimal getAmount(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (Exception e) {
            throw new WebApplicationException(Response.ok(createErrorResponse("Amount '" + amount + "' is not a number")).build());
        }
    }

    private BaseResponse createErrorResponse(String message) {
        BaseResponse response = new BaseResponse();
        response.setStatus(BaseResponse.Status.ERROR);
        response.setMessage(message);
        return response;
    }

    private AccountResponse createAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setBalance(account.getBalance().toPlainString());
        return response;
    }
}
