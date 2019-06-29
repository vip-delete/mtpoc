package com.example;

import com.example.dao.DataStore;
import com.example.dao.DataStoreImpl;
import com.example.view.AccountResponse;
import com.example.view.BaseResponse;
import com.example.view.MoneyRequest;
import com.example.view.TransferRequest;
import com.example.web.AccountControllerV1;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AccountControllerTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig()
                .register(AccountControllerV1.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(DataStoreImpl.class).to(DataStore.class);
                    }
                });
    }

    @Test
    public void test() {
        {
            // acc 1
            AccountResponse response = target("/api/v1/accounts").request().post(Entity.json(""), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(1L, response.getId());
        }
        {
            // acc 2
            AccountResponse response = target("/api/v1/accounts").request().post(Entity.json(""), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
        }
        {
            // balance 1
            AccountResponse response = target("/api/v1/accounts/1").request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(1L, response.getId());
            Assert.assertEquals("0", response.getBalance());
        }
        {
            // balance 2
            AccountResponse response = target("/api/v1/accounts/2").request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
            Assert.assertEquals("0", response.getBalance());
        }
        {
            // add 5 to acc 1
            MoneyRequest request = new MoneyRequest();
            request.setAmount("5");
            AccountResponse response = target("/api/v1/accounts/1/money").request().post(Entity.json(request), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(1L, response.getId());
            Assert.assertEquals("5", response.getBalance());
        }
        {
            // add 7 to acc 1
            MoneyRequest request = new MoneyRequest();
            request.setAmount("7");
            AccountResponse response = target("/api/v1/accounts/1/money").request().post(Entity.json(request), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(1L, response.getId());
            Assert.assertEquals("12", response.getBalance());
        }
        {
            // add 10 to acc 2
            MoneyRequest request = new MoneyRequest();
            request.setAmount("10");
            AccountResponse response = target("/api/v1/accounts/2/money").request().post(Entity.json(request), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
            Assert.assertEquals("10", response.getBalance());
        }
        {
            // withdraw 3 from acc 2
            MoneyRequest request = new MoneyRequest();
            request.setAmount("-3");
            AccountResponse response = target("/api/v1/accounts/2/money").request().post(Entity.json(request), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
            Assert.assertEquals("7", response.getBalance());
        }
        {
            // transfer 4 from 1 (balance 12) to 2 (balance 7)
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(2);
            request.setAmount("4");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
        }
        {
            // balance 1
            AccountResponse response = target("/api/v1/accounts/1").request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(1L, response.getId());
            Assert.assertEquals("8", response.getBalance());
        }
        {
            // balance 2
            AccountResponse response = target("/api/v1/accounts/2").request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
            Assert.assertEquals("11", response.getBalance());
        }
        {
            // transfer 100 from 2 to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(2);
            request.setToAccountId(2);
            request.setAmount("100");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
        }
        {
            // balance 2
            AccountResponse response = target("/api/v1/accounts/2").request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(2L, response.getId());
            Assert.assertEquals("11", response.getBalance());
        }
        {
            // transfer 0 from 1 to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(2);
            request.setAmount("0");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
        }

        // test errors
        {
            // balance of unknown
            BaseResponse response = target("/api/v1/accounts/3").request().get(BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("not found"));
        }

        {
            // add 10 to acc 2
            MoneyRequest request = new MoneyRequest();
            request.setAmount("10");
            BaseResponse response = target("/api/v1/accounts/-1/money").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("Can't add/withdraw"));
        }
        {
            // add abc to acc 1
            MoneyRequest request = new MoneyRequest();
            request.setAmount("abc");
            BaseResponse response = target("/api/v1/accounts/1/money").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("not a number"));
        }
        {
            // transfer abc from 1 to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(2);
            request.setAmount("abc");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("not a number"));
        }
        {
            // transfer -100 from 1 to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(2);
            request.setAmount("-100");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("Can't transfer"));
        }
        {
            // transfer 100 from 1 to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(2);
            request.setAmount("100");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("Can't transfer"));
        }
        {
            // transfer 100 from 1 to unknown
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(1);
            request.setToAccountId(200);
            request.setAmount("100");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("Can't transfer"));
        }
        {
            // transfer 100 from unknown to 2
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(100);
            request.setToAccountId(2);
            request.setAmount("100");
            BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
            Assert.assertEquals(BaseResponse.Status.ERROR, response.getStatus());
            Assert.assertThat(response.getMessage(), StringContains.containsString("Can't transfer"));
        }
    }

    @Test
    public void concurrentTest() throws Exception {
        // create 1000 acc
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            AccountResponse response = target("/api/v1/accounts").request().post(Entity.json(""), AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            ids.add(response.getId());
        }

        String freeMoney = "10000032423423400000000000.00000000034234000";
        {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (long id : ids) {
                tasks.add(() -> {
                    // add freeMoney to everyone
                    MoneyRequest request = new MoneyRequest();
                    request.setAmount(freeMoney);
                    AccountResponse response = target("/api/v1/accounts/" + id + "/money").request().post(Entity.json(request), AccountResponse.class);
                    Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
                    Assert.assertEquals(id, response.getId());
                    Assert.assertEquals(freeMoney, response.getBalance());
                    return null;
                });
            }
            ExecutorService service = Executors.newFixedThreadPool(100);
            for (Future<Void> f : service.invokeAll(tasks)) {
                f.get();
            }
        }

        {
            // transfer money between first 20 accounts 500 times
            Random random = new Random();
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                tasks.add(() -> {
                    // transfer 4 from 1 (balance 12) to 2 (balance 7)
                    int fromIdx = random.nextInt(20);
                    int toIdx = random.nextInt(20);
                    long from = ids.get(fromIdx);
                    long to = ids.get(toIdx);
                    BigDecimal amount = new BigDecimal(random.nextDouble() * (double) random.nextLong()).abs();
                    TransferRequest request = new TransferRequest();
                    request.setFromAccountId(from);
                    request.setToAccountId(to);
                    request.setAmount(amount.toPlainString());
                    BaseResponse response = target("/api/v1/transfers").request().post(Entity.json(request), BaseResponse.class);
                    Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
                    return null;
                });
            }
            ExecutorService service = Executors.newFixedThreadPool(100);
            for (Future<Void> f : service.invokeAll(tasks)) {
                f.get();
            }
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (long id : ids) {
            AccountResponse response = target("/api/v1/accounts/" + id).request().get(AccountResponse.class);
            Assert.assertEquals(BaseResponse.Status.OK, response.getStatus());
            Assert.assertEquals(id, response.getId());
            sum = sum.add(new BigDecimal(response.getBalance()));
        }
        BigDecimal expected = new BigDecimal(freeMoney).multiply(new BigDecimal(ids.size()));
        Assert.assertTrue("expected = " + expected.toPlainString() + ", actual=" + sum.toPlainString(), expected.compareTo(sum) == 0);
    }
}
