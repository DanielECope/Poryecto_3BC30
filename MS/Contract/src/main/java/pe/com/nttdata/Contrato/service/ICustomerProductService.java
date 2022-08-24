package pe.com.nttdata.Contrato.service;

import pe.com.nttdata.Contrato.model.CustomerProduct;

import java.time.LocalDate;
import java.util.List;

public interface ICustomerProductService extends ICRUD<CustomerProduct, String> {
	//Mono<CustomerProduct> findByCustomersIdAndProductId(String customersId, String productId);
	List<CustomerProduct> findByCustomersIdAndProductId(String customersId, String productId);
	CustomerProduct insert(CustomerProduct obj);
	List<CustomerProduct> findByCustomerId(String customerId);
	CustomerProduct associateDebitCardAndBankAccount(String accountBankAccount,String accountDebitCard);
	List<CustomerProduct> findByCustomersIdentificationDocument(String identificationDocument);

	List<CustomerProduct> findByProductIdAndRegisterDateBetween(String productId, LocalDate from, LocalDate to);
}
