package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminModerateProduitRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitResponse;
import com.pharmacopee.ladafura.enums.StatutProduit;

public interface IAdminProduitService {

    Page<AdminProduitResponse> getAllProduits(StatutProduit statut, Long categorieId, Pageable pageable);

    AdminProduitResponse getProduitById(Long id);

    AdminProduitResponse moderateProduit(Long id, AdminModerateProduitRequest request);

    List<AdminCategorieResponse> getAllCategories();

    AdminCategorieResponse createCategorie(AdminCategorieRequest request);

    AdminCategorieResponse updateCategorie(Long id, AdminCategorieRequest request);

    void deleteCategorie(Long id);
}
