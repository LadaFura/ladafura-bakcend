package com.pharmacopee.ladafura.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.EtudeScientifique;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationNomVernaculaireDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteLocaliteDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteMaladieDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteMediaDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;

@Component
public class PopulationPlanteMapper {

    public static final String AVERTISSEMENT_MEDICAL =
            "Les connaissances et usages traditionnels présentés ici sont issus du patrimoine ethnobotanique et culturel du Mali. " +
            "Ils ne constituent en aucun cas une preuve scientifique d'efficacité clinique ni une prescription médicale. " +
            "Consultez toujours un professionnel de santé qualifié.";

    public PopulationPlanteSummaryResponse toSummaryResponse(Plante plante, int nombreConnaissances, int nombreEtudes) {
        if (plante == null) {
            return null;
        }

        List<String> nomsVernaculaires = plante.getNomsPlante() != null
                ? plante.getNomsPlante().stream().map(NomPlante::getNom).collect(Collectors.toList())
                : List.of();

        List<String> maladies = plante.getMaladies() != null
                ? plante.getMaladies().stream().map(Maladie::getNom).collect(Collectors.toList())
                : List.of();

        return PopulationPlanteSummaryResponse.builder()
                .id(plante.getId())
                .nomScientifique(plante.getNomScientifique())
                .description(plante.getDescription())
                .photoUrl(plante.getPhotoUrl())
                .nomsVernaculaires(nomsVernaculaires)
                .maladies(maladies)
                .nombreConnaissances(nombreConnaissances)
                .nombreEtudesScientifiques(nombreEtudes)
                .build();
    }

    public PopulationPlanteDetailResponse toDetailResponse(
            Plante plante,
            List<VertuDeLaPlante> vertusValidees,
            List<EtudeScientifique> etudes,
            List<NomPlante> nomsVernaculaires) {

        if (plante == null) {
            return null;
        }

        // 1. Noms vernaculaires
        List<PopulationNomVernaculaireDto> nomsDto = nomsVernaculaires != null
                ? nomsVernaculaires.stream().map(this::toNomVernaculaireDto).collect(Collectors.toList())
                : List.of();

        // 2. Connaissances traditionnelles validées uniquement
        List<PopulationConnaissanceTraditionnelleDto> connaissancesDto = vertusValidees != null
                ? vertusValidees.stream().map(this::toConnaissanceDto).collect(Collectors.toList())
                : List.of();

        // 3. Études scientifiques
        List<PopulationEtudeScientifiqueDto> etudesDto = etudes != null
                ? etudes.stream().map(this::toEtudeDto).collect(Collectors.toList())
                : List.of();

        // 4. Maladies associées
        List<PopulationPlanteMaladieDto> maladiesDto = plante.getMaladies() != null
                ? plante.getMaladies().stream().map(this::toMaladieDto).collect(Collectors.toList())
                : List.of();

        // 5. Médias (Photo officielle + Photos/Audios des collectes validées associées)
        List<PopulationPlanteMediaDto> mediasDto = new ArrayList<>();
        if (plante.getPhotoUrl() != null && !plante.getPhotoUrl().isBlank()) {
            mediasDto.add(PopulationPlanteMediaDto.builder()
                    .typeMedia("PHOTO_OFFICIELLE")
                    .url(plante.getPhotoUrl())
                    .description("Spécimen officiel de référence")
                    .build());
        }

        // 6. Localités déduites des collectes validées (dédoublonnées)
        List<PopulationPlanteLocaliteDto> localitesDto = new ArrayList<>();
        Set<String> localiteKeys = new HashSet<>();
        Set<String> mediaUrls = new HashSet<>();

        if (vertusValidees != null) {
            for (VertuDeLaPlante vertu : vertusValidees) {
                Collecte collecte = vertu.getCollecte();
                if (collecte != null) {
                    // Médias terrain
                    if (collecte.getPhotoUrl() != null && !collecte.getPhotoUrl().isBlank() && mediaUrls.add(collecte.getPhotoUrl())) {
                        mediasDto.add(PopulationPlanteMediaDto.builder()
                                .typeMedia("PHOTO_TERRAIN")
                                .url(collecte.getPhotoUrl())
                                .description(collecte.getDescription() != null ? collecte.getDescription() : "Photo de terrain")
                                .build());
                    }
                    if (collecte.getAudioUrl() != null && !collecte.getAudioUrl().isBlank() && mediaUrls.add(collecte.getAudioUrl())) {
                        mediasDto.add(PopulationPlanteMediaDto.builder()
                                .typeMedia("AUDIO_TERRAIN")
                                .url(collecte.getAudioUrl())
                                .description("Témoignage oral associé à la collecte")
                                .build());
                    }

                    // Localisation
                    Localisation loc = collecte.getLocalisation();
                    if (loc != null) {
                        String key = String.format("%s-%s-%s-%s", loc.getRegion(), loc.getCercle(), loc.getCommune(), loc.getLocalite());
                        if (localiteKeys.add(key)) {
                            localitesDto.add(toLocaliteDto(loc));
                        }
                    }
                }
            }
        }

        return PopulationPlanteDetailResponse.builder()
                .id(plante.getId())
                .nomScientifique(plante.getNomScientifique())
                .description(plante.getDescription())
                .photoUrl(plante.getPhotoUrl())
                .avertissementMedical(AVERTISSEMENT_MEDICAL)
                .nomsVernaculaires(nomsDto)
                .connaissancesTraditionnelles(connaissancesDto)
                .etudesScientifiques(etudesDto)
                .maladiesAssociees(maladiesDto)
                .medias(mediasDto)
                .localites(localitesDto)
                .build();
    }

    public PopulationNomVernaculaireDto toNomVernaculaireDto(NomPlante nom) {
        if (nom == null) {
            return null;
        }
        return PopulationNomVernaculaireDto.builder()
                .id(nom.getId())
                .nom(nom.getNom())
                .langue(nom.getLangue())
                .pays(nom.getPays())
                .build();
    }

    public PopulationConnaissanceTraditionnelleDto toConnaissanceDto(VertuDeLaPlante vertu) {
        if (vertu == null) {
            return null;
        }
        return PopulationConnaissanceTraditionnelleDto.builder()
                .id(vertu.getId())
                .usageRapporte(vertu.getUsageRapporte())
                .partieUtilisee(vertu.getPartieUtilisee())
                .preparation(vertu.getPreparation())
                .precaution(vertu.getPrecaution())
                .description(vertu.getDescription())
                .build();
    }

    public PopulationEtudeScientifiqueDto toEtudeDto(EtudeScientifique etude) {
        if (etude == null) {
            return null;
        }
        return PopulationEtudeScientifiqueDto.builder()
                .id(etude.getId())
                .titre(etude.getTitre())
                .auteurs(etude.getAuteurs())
                .annee(etude.getAnnee())
                .reference(etude.getReference())
                .resume(etude.getResume())
                .documentUrl(etude.getDocumentUrl())
                .build();
    }

    public PopulationPlanteMaladieDto toMaladieDto(Maladie maladie) {
        if (maladie == null) {
            return null;
        }
        return PopulationPlanteMaladieDto.builder()
                .id(maladie.getId())
                .nom(maladie.getNom())
                .description(maladie.getDescription())
                .build();
    }

    public PopulationPlanteLocaliteDto toLocaliteDto(Localisation loc) {
        if (loc == null) {
            return null;
        }
        return PopulationPlanteLocaliteDto.builder()
                .region(loc.getRegion())
                .cercle(loc.getCercle())
                .commune(loc.getCommune())
                .localite(loc.getLocalite())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }
}
