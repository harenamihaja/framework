<!-- ===== form-upload.jsp - Upload simple ===== -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload de fichier</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input[type="text"], input[type="file"] { 
            width: 100%; 
            padding: 8px; 
            box-sizing: border-box; 
        }
        button { 
            background: #007bff; 
            color: white; 
            padding: 10px 20px; 
            border: none; 
            cursor: pointer; 
        }
        button:hover { background: #0056b3; }
    </style>
</head>
<body>
    <h1>Upload de fichier</h1>
    
    <!-- IMPORTANT : enctype="multipart/form-data" est OBLIGATOIRE -->
    <form action="${pageContext.request.contextPath}/upload" 
          method="post" 
          enctype="multipart/form-data">
        
        <div class="form-group">
            <label for="titre">Titre :</label>
            <input type="text" id="titre" name="titre" required>
        </div>
        
        <div class="form-group">
            <label for="fichier">Fichier :</label>
            <input type="file" id="fichier" name="fichier" required>
        </div>
        
        <button type="submit">Uploader</button>
    </form>
</body>
</html>


<!-- ===== form-upload-multiple.jsp - Upload multiple ===== -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload multiple</title>
</head>
<body>
    <h1>Upload de plusieurs fichiers</h1>
    
    <form action="${pageContext.request.contextPath}/upload-multiple" 
          method="post" 
          enctype="multipart/form-data">
        
        <div class="form-group">
            <label for="description">Description :</label>
            <input type="text" id="description" name="description">
        </div>
        
        <div class="form-group">
            <label for="fichiers">Fichiers (multiple) :</label>
            <!-- L'attribut "multiple" permet de sélectionner plusieurs fichiers -->
            <input type="file" id="fichiers" name="fichiers" multiple required>
        </div>
        
        <button type="submit">Uploader tous les fichiers</button>
    </form>
</body>
</html>


<!-- ===== form-upload-image.jsp - Upload avec prévisualisation ===== -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload d'image</title>
    <style>
        #preview { 
            max-width: 300px; 
            max-height: 300px; 
            margin-top: 10px; 
            display: none; 
        }
    </style>
</head>
<body>
    <h1>Upload d'image avec prévisualisation</h1>
    
    <form action="${pageContext.request.contextPath}/upload-image" 
          method="post" 
          enctype="multipart/form-data">
        
        <div class="form-group">
            <label for="image">Sélectionner une image :</label>
            <input type="file" 
                   id="image" 
                   name="image" 
                   accept="image/*" 
                   required
                   onchange="previewImage(event)">
        </div>
        
        <!-- Prévisualisation de l'image -->
        <img id="preview" alt="Prévisualisation">
        
        <button type="submit">Uploader l'image</button>
    </form>

    <script>
        function previewImage(event) {
            const preview = document.getElementById('preview');
            const file = event.target.files[0];
            
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                    preview.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        }
    </script>
</body>
</html>


<!-- ===== upload-success.jsp - Page de confirmation ===== -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload réussi</title>
    <style>
        .success { color: green; padding: 10px; background: #d4edda; }
        .error { color: red; padding: 10px; background: #f8d7da; }
    </style>
</head>
<body>
    <h1>Résultat de l'upload</h1>
    
    <c:if test="${not empty message}">
        <div class="success">
            <p>${message}</p>
            <p><strong>Nom du fichier :</strong> ${filename}</p>
            <p><strong>Taille :</strong> ${size} bytes</p>
            <p><strong>Titre :</strong> ${titre}</p>
        </div>
    </c:if>
    
    <c:if test="${not empty error}">
        <div class="error">
            <p>${error}</p>
        </div>
    </c:if>
    
    <p><a href="${pageContext.request.contextPath}/form-upload">Retour</a></p>
</body>
</html>