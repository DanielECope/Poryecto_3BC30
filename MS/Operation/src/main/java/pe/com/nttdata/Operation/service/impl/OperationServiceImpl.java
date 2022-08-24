package pe.com.nttdata.Operation.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pe.com.nttdata.Operation.exception.ModelNotFoundException;
import pe.com.nttdata.Operation.model.Customer;
import pe.com.nttdata.Operation.model.CustomerProduct;
import pe.com.nttdata.Operation.model.Operation;
import pe.com.nttdata.Operation.model.Product;
import pe.com.nttdata.Operation.repository.IOperationRepository;
import pe.com.nttdata.Operation.repository.IOperationRepositoryMongo;
import pe.com.nttdata.Operation.service.IOperationService;
import picocli.CommandLine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
public class OperationServiceImpl implements IOperationService {
	private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);

	private final WebClient webClientCustomer= WebClient.create("http://localhost:7070/api/1.0.0/customers");
	private final WebClient webClientProducts= WebClient.create("http://localhost:7070/api/1.0.0/products");
	private final WebClient webClientContract= WebClient.create("http://localhost:7070/api/1.0.0/contracts");

	@Autowired
	private IOperationRepository repo;
	@Autowired
	private IOperationRepositoryMongo repo2;

	@Autowired
	RestTemplate restTemplate;

	public OperationServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public Mono<CustomerProduct> findByIdContract(String id){
		logger.info("Class: OperationServiceImpl -> Method: findByIdContract ");
		return webClientContract.get().uri("/{id}",id)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(CustomerProduct.class);
	}
	@Override
	public CustomerProduct findByIdContract2(String id) {
		logger.info("Class: OperationServiceImpl -> Method: findByCustomersIdAndProductId2 ");
		logger.info("http://localhost:7073/api/1.0.0/contracts/fintById/"+id);
		CustomerProduct obj= restTemplate.getForObject("http://localhost:7073/api/1.0.0/contracts/fintById/"+id,CustomerProduct.class);
		logger.info(obj.toString());
		return obj;
	}
	public Product findByIdProduct2(String id){
		logger.info("Class: CustomerProductServiceImpl -> Method: findByIdProduct ");
		return restTemplate.getForObject("http://localhost:7072/api/1.0.0/products/"+id,Product.class);
	}
	public Customer findByIdCustomer2(String id){
		logger.info("Class: CustomerProductServiceImpl -> Method: findByIdCustomer2 "+"http://localhost:7071/api/1.0.0/customers/"+id);
		return restTemplate.getForObject("http://localhost:7071/api/1.0.0/customers/fintById/"+id,Customer.class);
	}

	public CustomerProduct updateContract2(CustomerProduct obj){
		logger.info("Class: OperationServiceImpl -> Method: updateContract2 ");
		restTemplate.put("http://localhost:7073/api/1.0.0/contracts/update",obj);
		return  obj;
	}
	public Mono<CustomerProduct> updateContract(CustomerProduct obj){
		logger.info("Class: OperationServiceImpl -> Method: updateContract ");
		return webClientContract.put().uri("/update",obj)
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(obj))
				.retrieve()
				.bodyToMono(CustomerProduct.class);
	}
	public CustomerProduct findByCustomersIdAndProductId2(String customersId, String productId) {
		logger.info("Class: OperationServiceImpl -> Method: findByCustomersIdAndProductId2 ");
		return restTemplate.getForObject("http://localhost:7070/api/1.0.0/contracts/findByCAndP?customersId="+customersId+"&productId="+productId,CustomerProduct.class);
	}
	public Mono<CustomerProduct> findByCustomersIdAndProductId(String customersId, String productId){
		logger.info("Class: OperationServiceImpl -> Method: findByCustomersIdAndProductId ");
		return webClientContract.get().uri("/findByCAndP?customersId="+customersId+"&productId="+productId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(CustomerProduct.class);
	}

	public Flux<Customer> findAllCustomer(){
		logger.info("Class: OperationServiceImpl -> Method: findAllCustomer ");
		return webClientCustomer.get().uri("")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Customer.class);
	}

	public Mono<Product> findByIdProduct(String id){
		logger.info("Class: OperationServiceImpl -> Method: findByIdProduct ");
		return webClientProducts.get().uri("/{id}",id)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Product.class);
	}

	@Override
	public Operation insert(Operation obj) {
		logger.info("Class: OperationServiceImpl -> Method: insert -> Params "+obj.toString());
		obj.setOperationType( obj.getOperationType().toUpperCase() );
		CustomerProduct	destinationAccount;
		CustomerProduct	originAccount;
		BigDecimal commision= BigDecimal.valueOf(0);
		try{
			logger.info("CUENTA DESTINO ::: "+findByIdContract(obj.getDestinationAccount()).toString());
			originAccount=findByIdContract2(obj.getOriginAccount());
			if (originAccount.getId()==null && !obj.getOperationType().equals("R")){
				logger.info("CUENTA ORIGEN NO EXISTE ::: "+obj.getOriginAccount());
				//throw new RuntimeException("CLIENTE TIENE NO REGISTRADO ::: "+obj.getOperationCustomerId());
				return  null;
			}

			destinationAccount=findByIdContract2(obj.getDestinationAccount());
			if (destinationAccount.getId()==null && !obj.getOperationType().equals("R")){
				logger.info("CUENTA DESTINO NO EXISTE ::: "+obj.getDestinationAccount());
				//throw new RuntimeException("CLIENTE TIENE NO REGISTRADO ::: "+obj.getOperationCustomerId());
				return  null;
			}

			if (originAccount.getNumberOfMoves()>= originAccount.getMaxNumberTransactionsNoCommissions()) {
				commision= BigDecimal.valueOf(originAccount.getProduct().getCommission());
			}

			if (obj.getCustomerId()==null){
				logger.info("CLIENTE TIENE NO REGISTRADO ::: "+obj.getOperationCustomerId());
				//throw new RuntimeException("CLIENTE TIENE NO REGISTRADO ::: "+obj.getOperationCustomerId());
				//return  null;
			}
			obj.setCustomerId(obj.getCustomerId());



			if(obj.getOperationType().equals("D"))//deposito
			{
				logger.info("ADD ::: "+obj.getAmount().toString());
				destinationAccount.setAmountAvailable(destinationAccount.getAmountAvailable().add(obj.getAmount()).add(commision));
			}
			else if(obj.getOperationType().equals("R"))//retiro
			{
				logger.info("SUBTRACT ::: "+obj.getAmount().toString());
				destinationAccount.setAmountAvailable(destinationAccount.getAmountAvailable().subtract(obj.getAmount()).subtract(commision));
			}
			else if(obj.getOperationType().equals("P") && obj.getCustomerProduct().getProduct().getName().equals("CREDIT"))//pago
				destinationAccount.setCreditLine(destinationAccount.getAmountAvailable().subtract(obj.getAmount()).subtract(commision));
			else if (obj.getOperationType().equals("T")) {
				if (originAccount.getAmountAvailable().compareTo(obj.getAmount()) < 0){
					logger.info("SALDO INSUFICIENTE EN SU CUENTA ::: SU MONTO DISPONIBLE ES:"+originAccount.getAmountAvailable());
					throw new RuntimeException("SALDO INSUFICIENTE EN SU CUENTA ::: SU MONTO DISPONIBLE ES:"+originAccount.getAmountAvailable());
					//return  null;
				}
				originAccount.setAmountAvailable(originAccount.getAmountAvailable().subtract(obj.getAmount()).subtract(commision));
				obj.setCustomerId(originAccount.getCustomerId());

			}


			BigDecimal commisionConvert = BigDecimal.valueOf(destinationAccount.getProduct().getCommission());
			if ( destinationAccount.getNumberOfMoves() >=destinationAccount.getMaxNumberTransactionsNoCommissions())
				destinationAccount.setAmountAvailable( destinationAccount.getAmountAvailable().add(commisionConvert) );

			destinationAccount.setNumberOfMoves(destinationAccount.getNumberOfMoves()+1);
			destinationAccount.setId(obj.getOriginAccount());//cambio de cuenta
			logger.info("CUENTA DESTINO DATOS ::: "+destinationAccount.toString());
			logger.info("CUENTA ORIGEN DATOS ::: "+originAccount.toString());
			CustomerProduct contract2=this.updateContract2(destinationAccount);
			//destinationAccount=this.updateContract2(destinationAccount);
			if (!obj.getOperationType().equals("R") && !(obj.getDestinationAccount().equals(obj.getOriginAccount()))){
				logger.info("SE ACTUALIZA LA CUENTA ORIGEN ::: "+originAccount.getId());
				originAccount=this.updateContract2(originAccount);
			}
			logger.info("guardando....!!!");
			obj.setCustomerProductId(obj.getDestinationAccount());
			obj.setRegisterDate(LocalDateTime.now());
			obj.setOperationCustomerId(destinationAccount.getId());
			obj.setCustomerId(originAccount.getCustomerId());
			obj=repo2.save(obj);
		}catch (Exception e){
			logger.info(e.getMessage());
		}
		return obj;
	}

	@Override
	public Mono<Operation> update(Operation obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<Operation> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Operation> findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Void> delete(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<Operation> findByIdentificationDocumentAndProductId(String identificationDocument, String productId) {
		return this.findAllCustomer()
				.filter(customers -> customers.getIdentificationDocument().equals(identificationDocument))
				.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("CLIENTE NO ENCONTRADO")))
				.next()
				.flatMapMany(customer -> {
					return this.findByIdProduct(productId)
							.flatMapMany(product -> {
								return this.findByCustomersIdAndProductId(customer.getId(), productId)
										.flatMapMany(contract -> {
											return repo.findAll()
													.filter(operations -> operations.getCustomerProductId().equals(contract.getId()))
													.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("MOVIMIENTO NO ENCONTRADO")))
													.map(o -> {
														contract.setCustomers(customer);
														contract.setProduct(product);
														o.setCustomerProduct(contract);
														return o;
													})
													.doOnNext(o -> logger.info("SE ENCONTRÓ LAS MOVIMIENTOS DEL CLIENTE ::: " + customer.getFullName()));
										});
							});
				});
	}

}
