package br.com.tokiomarine.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.tokiomarine.domain.Cliente;
import br.com.tokiomarine.domain.Endereco;
import br.com.tokiomarine.repository.ClienteRepository;
import br.com.tokiomarine.repository.EnderecoRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("clientes")
public class ClienteController {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping()
	public Iterable<Cliente> listarClientes() {
		return clienteRepository.findAll();
	}

	@PostMapping()
	public ResponseEntity<Void> incluirCliente(@Valid @RequestBody Cliente cliente) {
		cliente.setId(null);

		Cliente cli = clienteRepository.save(cliente);

		cliente.getListEndereco().forEach(c -> {
			Endereco e = this.restTemplate.getForObject("https://viacep.com.br/ws/" + c.getCep() + "/json/",
					Endereco.class);
			c.setBairro(e.getBairro());
			c.setDdd(e.getDdd());
			c.setGia(e.getGia());
			c.setIbge(e.getIbge());
			c.setLocalidade(e.getLocalidade());
			c.setLogradouro(e.getLogradouro());
			c.setSiafi(e.getSiafi());
			c.setUf(e.getUf());
			c.setCliente(cli);
		});
		enderecoRepository.saveAll(cliente.getListEndereco());

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cliente.getId())
				.toUri();
		return ResponseEntity.created(uri).build();
	}

	@PutMapping()
	public ResponseEntity<Void> editarCliente(@Valid @RequestBody Cliente cliente) {
		Cliente cli = clienteRepository.save(cliente);
		cliente.getListEndereco().forEach(c -> {
			c.setCliente(cli);
		});
		enderecoRepository.saveAll(cliente.getListEndereco());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
		clienteRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<Cliente> findByIdCliente(@PathVariable Long id) throws NotFoundException {
		return ResponseEntity.ok(this.clienteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cliente " + id + " não encontrado!")));
	}

	@GetMapping(value = "/page")
	public ResponseEntity<Page<Cliente>> findPage(@RequestParam("search") String search,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "linesPerPage", required = false, defaultValue = "24") Integer linesPerPage) {
		log.debug(search);
		// PageRequest pageRequest = new PageRequest(page, linesPerPage,
		// Direction.valueOf(direction), orderBy);
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Sort.Direction.ASC, "nome");

		Page<Cliente> list = clienteRepository.findClientePage(search, pageRequest);

		return ResponseEntity.ok().body(list);
	}

}
