<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ajouter un produit - Sprint 11 - Session</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            padding: 40px;
            max-width: 500px;
            width: 100%;
        }
        
        h1 {
            color: #333;
            margin-bottom: 10px;
            text-align: center;
            font-size: 28px;
        }
        
        .subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 30px;
            font-size: 14px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
            font-size: 14px;
        }
        
        input[type="text"],
        input[type="number"] {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        
        input[type="text"]:focus,
        input[type="number"]:focus {
            outline: none;
            border-color: #667eea;
            background-color: #f8f9ff;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }
        
        button,
        a.btn {
            flex: 1;
            padding: 12px;
            border: none;
            border-radius: 5px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-align: center;
            text-decoration: none;
            display: inline-block;
        }
        
        button[type="submit"] {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        button[type="submit"]:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        a.btn {
            background: #f0f0f0;
            color: #333;
            border: 1px solid #ddd;
        }
        
        a.btn:hover {
            background: #e0e0e0;
        }
        
        .info-box {
            background: #f0f7ff;
            border-left: 4px solid #667eea;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
            font-size: 13px;
            color: #666;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🛒 Panier</h1>
        <p class="subtitle">Sprint 11 - Test de Session avec @Session</p>
        
        <div class="info-box">
            <strong>ℹ️ Comment ça marche:</strong><br>
            Vous pouvez ajouter des produits au panier. <br>
            Les produits sont stockés dans la session HTTP grâce à l'annotation <code style="background: #fff; padding: 2px 6px;">@Session Map&lt;String, Object&gt;</code>
        </div>
        
        <form action="ajouter" method="POST">
            <div class="form-group">
                <label for="nom">Nom du produit <span style="color: red;">*</span></label>
                <input type="text" id="nom" name="nom" required placeholder="Ex: Livre, Téléphone, etc.">
            </div>
            
            <div class="form-group">
                <label for="prix">Prix <span style="color: red;">*</span></label>
                <input type="number" id="prix" name="prix" step="0.01" min="0" required placeholder="Ex: 29.99">
            </div>
            
            <div class="button-group">
                <button type="submit">Ajouter au panier</button>
                <a href="afficher" class="btn">Voir panier</a>
            </div>
        </form>
    </div>
</body>
</html>
