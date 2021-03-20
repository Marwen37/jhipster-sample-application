package com.mycompany.org.service.mapper;


import com.mycompany.org.domain.*;
import com.mycompany.org.service.dto.ProduitDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Produit} and its DTO {@link ProduitDTO}.
 */
@Mapper(componentModel = "spring", uses = {CategorieMapper.class})
public interface ProduitMapper extends EntityMapper<ProduitDTO, Produit> {

    @Mapping(source = "categorie.id", target = "categorieId")
    ProduitDTO toDto(Produit produit);

    @Mapping(source = "categorieId", target = "categorie")
    Produit toEntity(ProduitDTO produitDTO);

    default Produit fromId(Long id) {
        if (id == null) {
            return null;
        }
        Produit produit = new Produit();
        produit.setId(id);
        return produit;
    }
}
