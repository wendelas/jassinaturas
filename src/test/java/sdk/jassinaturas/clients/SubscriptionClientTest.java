package sdk.jassinaturas.clients;

import org.junit.Test;
import sdk.jassinaturas.Assinaturas;
import sdk.jassinaturas.clients.attributes.Address;
import sdk.jassinaturas.clients.attributes.Authentication;
import sdk.jassinaturas.clients.attributes.BestInvoiceDate;
import sdk.jassinaturas.clients.attributes.BillingInfo;
import sdk.jassinaturas.clients.attributes.Birthdate;
import sdk.jassinaturas.clients.attributes.Country;
import sdk.jassinaturas.clients.attributes.CreditCard;
import sdk.jassinaturas.clients.attributes.Customer;
import sdk.jassinaturas.clients.attributes.Invoice;
import sdk.jassinaturas.clients.attributes.Month;
import sdk.jassinaturas.clients.attributes.Plan;
import sdk.jassinaturas.clients.attributes.State;
import sdk.jassinaturas.clients.attributes.Subscription;
import sdk.jassinaturas.clients.attributes.SubscriptionStatus;
import sdk.jassinaturas.communicators.SandboxCommunicator;
import sdk.jassinaturas.exceptions.ApiResponseErrorException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sdk.jassinaturas.clients.attributes.SubscriptionStatus.CANCELED;

public class SubscriptionClientTest {

    private final Assinaturas assinaturas = new Assinaturas(new Authentication("JOSOAPZJ4JI3IQTRUUTIGWQEPRPMDW58",
            "Q1MSGUKMXXQTKO4W7OHHINJNFYSOCT4FJLJKYXKH"), new SandboxCommunicator());

    @Test
    public void shouldCreateANewSubscription() {
        Subscription toBeCreated = new Subscription();
        toBeCreated
                .withCode("sub_" + System.currentTimeMillis())
                .withAmount(100)
                .withPlan(new Plan().withCode("plan003"))
                .withCustomer(
                        new Customer()
                                .withCode("customer_created_with_subscription_" + System.currentTimeMillis())
                                .withBirthdate(new Birthdate().withDay(13).withMonth(Month.OCTOBER).withYear(1989))
                                .withCpf("12312312312")
                                .withEmail("teste@teste.com")
                                .withFullname("Danillo Souza")
                                .withPhoneAreaCode("11")
                                .withPhoneNumber("912341234")
                                .withAddress(
                                        new Address().withCity("São Paulo").withComplement("Apto")
                                                .withCountry(Country.BRA).withDistrict("Centro").withNumber("1000")
                                                .withState(State.SP).withStreet("9 de Julho").withZipcode("10012345"))
                                .withBillingInfo(
                                        new BillingInfo().withCreditCard(new CreditCard().withExpirationMonth("10")
                                                .withExpirationYear("18").withHolderName("Danillo Souza")
                                                .withNumber("4111111111111111"))));

        Subscription created = assinaturas.subscriptions().create(toBeCreated);

        assertEquals("Assinatura criada com sucesso", created.getMessage());

        assertEquals(created.getAmount(), 100);
        assertEquals(created.getPlan().getName(), "Plano Especial");
        assertEquals(created.getPlan().getCode(), "plan003");
        assertEquals(created.getStatus(), SubscriptionStatus.ACTIVE);
        assertEquals(created.getInvoice().getAmount(), 600);
        assertEquals(created.getInvoice().getStatus().getDescription(), "Aguardando confirmação");
        assertEquals(created.getInvoice().getStatus().getCode(), 2);
        assertEquals(created.getCustomer().getEmail(), "teste@teste.com");
        assertEquals(created.getCustomer().getFullname(), "Danillo Souza");
    }

    @Test
    public void shouldCreateANewSubscriptionWithoutANewCustomer() {
        Subscription toBeCreated = new Subscription();
        toBeCreated.withCode("subscription0000_" + System.currentTimeMillis())
                .withAmount(100)
                .withCustomer(new Customer().withCode("customer_created_with_subscription_1484075655594"))
                .withPlan(new Plan().withCode("plan003"));

        Subscription created = assinaturas.subscriptions().create(toBeCreated);

        assertEquals("Assinatura criada com sucesso", created.getMessage());

        assertEquals(created.getAmount(), 100);
        assertEquals(created.getPlan().getName(), "Plano Especial");
        assertEquals(created.getPlan().getCode(), "plan003");
        assertEquals(created.getStatus(), SubscriptionStatus.ACTIVE);
        assertEquals(created.getInvoice().getAmount(), 600);
        assertEquals(created.getInvoice().getStatus().getDescription(), "Aguardando confirmação");
        assertEquals(created.getInvoice().getStatus().getCode(), 2);
        assertNotNull(created.getCode());
        assertEquals(created.getCustomer().getEmail(), "teste@teste.com");
        assertEquals(created.getCustomer().getCode(), "customer_created_with_subscription_1484075655594");
        assertEquals(created.getCustomer().getFullname(), "Danillo Souza");
    }

    @Test
    public void shouldListAllSubscriptions() {

        List<Subscription> subscriptions = assinaturas.subscriptions().list();
        assertNotNull(subscriptions);
    }

    @Test
    public void shouldReturnErrors() {
        Subscription toBeCreated = new Subscription();
        toBeCreated.withCode("subscription00001").withAmount(100)
                .withCustomer(new Customer().withCode("customer000000001")).withPlan(new Plan().withCode("plan001"));

        try {
            Subscription created = assinaturas.subscriptions().create(toBeCreated);
            fail("Should return ApiResponseError");
        } catch (ApiResponseErrorException e) {
            assertEquals("Erro na requisição", e.getApiResponseError().getMessage());
            assertEquals("Código da assinatura já utilizado. Escolha outro código", e.getApiResponseError().getErrors()
                    .get(0).getDescription());
            assertEquals("MA92", e.getApiResponseError().getErrors().get(0).getCode());
        }
    }

    @Test
    public void shouldReturnIn1voicesFromSubscription() {
        List<Invoice> invoices = assinaturas.subscriptions().invoices("subscription00001");
        Invoice invoice = invoices.get(0);

        assertEquals(10, invoice.getCreationDate().getMinute());
        assertEquals(39, invoice.getCreationDate().getSecond());
        assertEquals(Month.JANUARY, invoice.getCreationDate().getMonth());
        assertEquals(2017, invoice.getCreationDate().getYear());
        assertEquals(10, invoice.getCreationDate().getHour());
        assertEquals(10, invoice.getCreationDate().getDay());

        assertEquals(600, invoice.getAmount());
        assertEquals("subscription00001", invoice.getSubscriptionCode());
        assertEquals(1, invoice.getOccurrence());
        assertEquals(1269135, invoice.getId());

        assertEquals("Aguardando confirmação", invoice.getStatus().getDescription());
        assertEquals(2, invoice.getStatus().getCode());
    }

    @Test
    public void shouldShowASubscription() {

        Subscription subscription = assinaturas.subscriptions().show("Teste_1484071813");

        assertEquals(10, subscription.getCreationDate().getMinute());
        assertEquals(14, subscription.getCreationDate().getSecond());
        assertEquals(Month.JANUARY, subscription.getCreationDate().getMonth());
        assertEquals(2017, subscription.getCreationDate().getYear());
        assertEquals(16, subscription.getCreationDate().getHour());
        assertEquals(10, subscription.getCreationDate().getDay());

        assertEquals(990, subscription.getAmount());
        assertEquals(CANCELED, subscription.getStatus());
        assertEquals("Teste_1484071813", subscription.getCode());

        assertEquals("1484071813@exemplo.com.br", subscription.getCustomer().getEmail());
        assertEquals("1484071813", subscription.getCustomer().getCode());
        assertEquals("Jose silva", subscription.getCustomer().getFullname());

        assertEquals("Plano Especial", subscription.getPlan().getName());
        assertEquals("plan003", subscription.getPlan().getCode());

        assertEquals(Month.APRIL, subscription.getExpirationDate().getMonth());
        assertEquals(2020, subscription.getExpirationDate().getYear());
        assertEquals(24, subscription.getExpirationDate().getDay());
    }

    @Test
    public void shouldCreateASubscriptionWithMonthlyPlanWithProRata() {
        Subscription subscription = new Subscription()
                .withCode("subscription_" + System.currentTimeMillis())
                .withAmount(100)
                .withCustomer(new Customer().withCode("customer_created_with_subscription_1484075655594"))
                .withPlan(new Plan().withCode("monthly_plan"))
                .withProRata(true)
                .withBestInvoiceDate(new BestInvoiceDate().withDayOfMonth(10));

        Subscription created = assinaturas.subscriptions().create(subscription);

        assertEquals("Assinatura criada com sucesso", created.getMessage());

        assertTrue(created.isProRata());
        assertEquals(10, created.getBestInvoiceDate().getDayOfMonth(), 0.0001);
    }

    @Test
    public void shouldCreateASubscriptionWithAnnualPlanWithProRata() {
        Subscription subscription = new Subscription()
                .withCode("subscription_" + System.currentTimeMillis())
                .withAmount(100)
                .withCustomer(new Customer().withCode("customer_created_with_subscription_1484075655594"))
                .withPlan(new Plan().withCode("annual_plan"))
                .withProRata(true)
                .withBestInvoiceDate(new BestInvoiceDate()
                        .withDayOfMonth(10)
                        .withMonthOfYear(10));

        Subscription created = assinaturas.subscriptions().create(subscription);

        assertEquals("Assinatura criada com sucesso", created.getMessage());

        assertTrue(created.isProRata());
        assertEquals(10, created.getBestInvoiceDate().getDayOfMonth(), 0.0001);
        assertEquals(10, created.getBestInvoiceDate().getMonthOfYear(), 0.0001);
    }

    @Test
    public void shouldGetResultFromToString() {
        String subscription = assinaturas.subscriptions().show("jassinaturas_show").toString();

        assertEquals(
                "Subscription{alerts=null, amount=3100, code='jassinaturas_show', creationDate=CreationDate [day=11, hour=7, minute=43, month=1, second=21, year=2017], customer=Customer [address=null, billingInfo=null, birthdate=null, code=1484127800, cpf=null, customers=null, email=1484127800@exemplo.com.br, fullname=Jose silva, message=null, phoneAreaCode=null, phoneNumber=null, birthdateDay=0, birthdateMonth=0, birthdateYear=0], expirationDate=ExpirationDate [day=11, month=JANUARY, year=2018], invoice=null, invoices=null, message='null', nextInvoiceDate=NextInvoiceDate [day=11, month=1, year=2017], plan=Plan [alerts=null, amount=0, billingCycles=0, code=monthly_plan, description=null, interval=null, maxQty=0, message=null, name=Plano Especial, plans=null, setupFee=0, status=null, trial=null], status=ACTIVE, subscriptions=null, coupon=null, proRata=null, bestInvoiceDate=null}",
                subscription);
    }

}
