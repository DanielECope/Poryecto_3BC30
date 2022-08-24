package pe.com.nttdata.Operation.controller;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.annotation.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import pe.com.nttdata.Operation.model.Customer;
import pe.com.nttdata.Operation.model.CustomerProduct;
import pe.com.nttdata.Operation.model.Operation;
import pe.com.nttdata.Operation.service.IOperationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/1.0.0/operations")
public class OperationController {
	
	@Autowired
	private IOperationService service;

	@GetMapping("/checkAvailableBalance/{cuenta}")
	public Map<String,String> checkAvailableBalance(@PathVariable String cuenta){
		log.info("CLASS ::: OperationController - METHOD :::checkAvailableBalance - PARAMS ::: "+cuenta);
		//consultar el saldo disponible
		CustomerProduct contract = service.findByIdContract2(cuenta);
		HashMap<String, String> map = new HashMap<>();
		map.put("cuenta", contract.getId());
		map.put("Monto disponible", contract.getAmountAvailable().toString());
		map.put("Fecha de contrato",contract.getRegisterDate().toString());
		if (contract.getProduct().getProductType().getName().equals("CREDIT")){
			map.put("línea de crédito",contract.getCreditLine().toString());
			map.put("fecha de pago",contract.getPaymentDate().toString());
		}
		//return "cuenta: "+ contract.getId();
		return map;
	}

	@PostMapping
	public Operation insert(@RequestBody Operation obj){
		log.info("CLASS ::: OperationController - METHOD :::INSERT - PARAMS ::: "+obj.toString());
		Operation operation = service.insert(obj);
		//return Mono.just(new ResponseEntity<Mono<Operation>>(operation, HttpStatus.CREATED));
		return operation;
	}
	
	@GetMapping("/{identificationDocument}/{productId}/")
	public Mono<ResponseEntity<Flux<Operation>>> findByIdentificationDocumentAndProductId(
			@PathVariable("identificationDocument") String identificationDocument,
			@PathVariable("productId") String productId){
		log.info("doc: "+identificationDocument);
		log.info("producto: "+productId);
		Flux<Operation> operations = service.findByIdentificationDocumentAndProductId(identificationDocument, productId);
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(operations));
	}

	
}
