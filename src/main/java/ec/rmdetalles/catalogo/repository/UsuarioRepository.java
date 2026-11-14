package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> { }
