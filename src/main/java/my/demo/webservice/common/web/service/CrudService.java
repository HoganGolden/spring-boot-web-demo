package my.demo.webservice.common.web.service;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface CrudService<DOMAIN> {

	@NonNull
	List<DOMAIN> listAll();
	
	@NonNull
	List<DOMAIN> listAll(@NonNull Sort sort);

	@NonNull
	List<DOMAIN> listAllBySpec(Specification<DOMAIN> spec);

	@NonNull
	List<DOMAIN> listAllByExample(DOMAIN domain);

	@NonNull
	List<DOMAIN> listAllByExample(DOMAIN domain, Sort sort);

	@NonNull
	Page<DOMAIN> listAndCountAll(@NonNull Pageable pageable);

	@NonNull
	Page<DOMAIN> listAndCountAll(Specification<DOMAIN> spec, @NonNull Pageable pageable);

	@NonNull
	Page<DOMAIN> listAndCountAll(DOMAIN domain, @NonNull Pageable pageable);

	DOMAIN getBySpec(Specification<DOMAIN> spec);

	DOMAIN getByExample(@NonNull DOMAIN domain);
	
	DOMAIN getByExample(@NonNull DOMAIN domain, Sort sort);

	boolean exists(Specification<DOMAIN> spec);

	boolean exists(DOMAIN domain);
	

	long count();
	
	long countByExample(DOMAIN domain);
	
	@NonNull
    @Transactional
    DOMAIN create(@NonNull DOMAIN domain);
	
	@NonNull
    @Transactional
    List<DOMAIN> createInBatch(@NonNull Collection<DOMAIN> domains);
	
	@NonNull
    @Transactional
    DOMAIN update(@NonNull DOMAIN domain);
	
	void flush();
	
	@NonNull
    @Transactional
    List<DOMAIN> updateInBatch(@NonNull Collection<DOMAIN> domains);

	@NonNull
	@Transactional
	void removeByExample(@NonNull DOMAIN domain);
	
	@Transactional
    void remove(@NonNull DOMAIN domain);

	@Transactional
    void removeAll(@NonNull Collection<DOMAIN> domains);
	
	@Transactional
    void removeAll();
}
