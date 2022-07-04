package my.demo.webservice.common.web.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface BaseRepository<DOMAIN, ID> extends JpaRepository<DOMAIN, ID>, JpaSpecificationExecutor<DOMAIN> {

    List<DOMAIN> findAll();

    List<DOMAIN> findAll(Sort sort);

    Page<DOMAIN> findAll(Pageable pageable);

    long count();
}