package controller;


import com.monframework.annotations.*;
import com.monframework.models.ModelView;
import com.monframework.models.UploadedFile;
import com.monframework.annotations.RequestParam;
import com.monframework.annotations.FileUpload;
import com.monframework.annotations.*;
import com.monframework.annotations.PostMapping;
import com.monframework.annotations.GetMapping;
import jakarta.servlet.http.HttpServletRequest;


import java.util.ArrayList;
import java.util.List;

@Controller
public class FileUploadController {

    // === EXEMPLE 1 : Upload simple d'un fichier ===
    @PostMapping("/upload")
    public ModelView uploadFile(
            HttpServletRequest request,
            @RequestParam("titre") String titre,
            @FileUpload("fichier") UploadedFile file) {
        
        ModelView mv = new ModelView("views/upload-success.jsp");
        
        try {
            if (file != null) {
                // Récupérer le chemin absolu du dossier uploads
                String uploadPath = request.getServletContext().getRealPath("/") + "uploads";
                
                // Sauvegarder le fichier
                String savedPath = file.saveTo(uploadPath);
                
                System.out.println("Fichier uploadé : " + file.getFilename());
                System.out.println("Taille : " + file.getSize() + " bytes");
                System.out.println("Type : " + file.getContentType());
                System.out.println("Sauvegardé dans : " + savedPath);
                
                mv.addObject("message", "Fichier uploadé avec succès !");
                mv.addObject("filename", file.getFilename());
                mv.addObject("size", file.getSize());
                mv.addObject("titre", titre);
            } else {
                mv.addObject("message", "Aucun fichier sélectionné");
            }
        } catch (Exception e) {
            mv.addObject("error", "Erreur lors de l'upload : " + e.getMessage());
            e.printStackTrace();
        }
        
        return mv;
    }

    // === EXEMPLE 2 : Upload multiple ===
    @PostMapping("/upload-multiple")
    public ModelView uploadMultipleFiles(
            HttpServletRequest request,
            @RequestParam("description") String description,
            @FileUpload("fichiers") List<UploadedFile> files) {
        
        ModelView mv = new ModelView("views/upload-multiple-success.jsp");
        
        try {
            String uploadPath = request.getServletContext().getRealPath("/") + "uploads";
            List<String> uploadedFiles = new ArrayList<>();
            
            for (UploadedFile file : files) {
                String savedPath = file.saveTo(uploadPath);
                uploadedFiles.add(file.getFilename());
                System.out.println("Fichier uploadé : " + file.getFilename());
            }
            
            mv.addObject("message", files.size() + " fichier(s) uploadé(s)");
            mv.addObject("files", uploadedFiles);
            mv.addObject("description", description);
            
        } catch (Exception e) {
            mv.addObject("error", "Erreur : " + e.getMessage());
        }
        
        return mv;
    }

    // === EXEMPLE 3 : Upload avec renommage ===
    @PostMapping("/upload-employe")
    public ModelView uploadEmployePhoto(
            HttpServletRequest request,
            @RequestParam("employeId") int employeId,
            @FileUpload(value = "photo", required = true) UploadedFile photo) {
        
        ModelView mv = new ModelView("views/employe-photo-success.jsp");
        
        try {
            String uploadPath = request.getServletContext().getRealPath("/") + "uploads/employes";
            
            // Renommer le fichier avec l'ID de l'employé
            String extension = photo.getFilename().substring(
                photo.getFilename().lastIndexOf("."));
            String customName = "employe_" + employeId + extension;
            
            String savedPath = photo.saveAs(uploadPath, customName);
            
            mv.addObject("message", "Photo de profil mise à jour !");
            mv.addObject("employeId", employeId);
            mv.addObject("photoPath", "uploads/employes/" + customName);
            
        } catch (Exception e) {
            mv.addObject("error", "Erreur : " + e.getMessage());
        }
        
        return mv;
    }

    // === EXEMPLE 4 : Afficher le formulaire d'upload ===
    @GetMapping("/form-upload")
    public ModelView showUploadForm() {
        return new ModelView("views/form-upload.jsp");
    }

    // === EXEMPLE 5 : Upload avec validation de type ===
    @PostMapping("/upload-image")
    public ModelView uploadImage(
            HttpServletRequest request,
            @FileUpload("image") UploadedFile image) {
        
        ModelView mv = new ModelView("views/upload-result.jsp");
        
        try {
            if (image == null) {
                mv.addObject("error", "Aucune image sélectionnée");
                return mv;
            }
            
            // Vérifier le type de fichier
            String contentType = image.getContentType();
            if (!contentType.startsWith("image/")) {
                mv.addObject("error", "Le fichier doit être une image");
                return mv;
            }
            
            // Vérifier la taille (max 5MB)
            if (image.getSize() > 5 * 1024 * 1024) {
                mv.addObject("error", "L'image ne doit pas dépasser 5MB");
                return mv;
            }
            
            String uploadPath = request.getServletContext().getRealPath("/") + "uploads/images";
            String savedPath = image.saveTo(uploadPath);
            
            mv.addObject("success", true);
            mv.addObject("imagePath", "uploads/images/" + image.getFilename());
            
        } catch (Exception e) {
            mv.addObject("error", "Erreur : " + e.getMessage());
        }
        
        return mv;
    }
}