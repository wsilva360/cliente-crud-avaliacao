package br.com.tokiomarine.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.tokiomarine.domain.Cliente;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {
	List<Cliente> findAllByOrderByNomeAsc();

	@Query("FROM Cliente c WHERE LOWER(c.nome) like %:search%")
	Page<Cliente> findClientePage(@Param("search") String searchTerm, Pageable pageRequest);
}
