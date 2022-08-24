package pe.com.nttdata.Operation.service;


import org.springframework.web.bind.annotation.PathVariable;
import pe.com.nttdata.Operation.model.CustomerProduct;
import pe.com.nttdata.Operation.model.Operation;
import reactor.core.publisher.Flux;

public interface IOperationService extends ICRUD<Operation, String> {
	Flux<Operation> findByIdentificationDocumentAndProductId(String identificationDocument, String productId);
	CustomerProduct findByIdContract2(String cuenta);
}
