<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panier - Sprint 11 - Session</title>
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
            padding: 20px;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            padding: 40px;
        }
        
        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 32px;
        }
        
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 14px;
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
        
        .panier-stats {
            display: flex;
            gap: 20px;
            margin-bottom: 30px;
            padding: 20px;
            background: #f9f9f9;
            border-radius: 5px;
        }
        
        .stat {
            flex: 1;
            text-align: center;
        }
        
        .stat-number {
            font-size: 28px;
            font-weight: bold;
            color: #667eea;
        }
        
        .stat-label {
            color: #666;
            font-size: 12px;
            margin-top: 5px;
        }
        
        .message {
            padding: 15px;
            background: #f0f7ff;
            border: 1px solid #667eea;
            border-radius: 5px;
            color: #333;
            margin-bottom: 20px;
            text-align: center;
            font-weight: 500;
        }
        
        .message.empty {
            background: #fff3cd;
            border-color: #ffc107;
            color: #856404;
        }
        
        .articles {
            margin-bottom: 30px;
        }
        
        .article-item {
            padding: 15px;
            border: 1px solid #eee;
            border-radius: 5px;
            margin-bottom: 10px;
            background: #fafafa;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .article-item:hover {
            background: #f5f5f5;
            border-color: #ddd;
        }
        
        .article-content {
            flex: 1;
        }
        
        .article-name {
            font-weight: 600;
            color: #333;
            font-size: 16px;
        }
        
        .article-price {
            color: #667eea;
            font-weight: bold;
            font-size: 16px;
            margin-left: 20px;
        }
        
        .info-dernier {
            padding: 15px;
            background: #e8f5e9;
            border-left: 4px solid #4caf50;
            margin-bottom: 20px;
            border-radius: 5px;
            font-size: 13px;
            color: #2e7d32;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            justify-content: center;
        }
        
        button,
        a.btn {
            padding: 12px 24px;
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
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        .btn-danger {
            background: #ff6b6b;
            color: white;
        }
        
        .btn-danger:hover {
            background: #ff5252;
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(255, 107, 107, 0.3);
        }
        
        .btn-secondary {
            background: #f0f0f0;
            color: #333;
            border: 1px solid #ddd;
        }
        
        .btn-secondary:hover {
            background: #e0e0e0;
        }
        
        .empty-panier {
            text-align: center;
            padding: 40px 20px;
            color: #999;
        }
        
        .empty-panier-icon {
            font-size: 48px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🛒 Mon Panier</h1>
        <p class="subtitle">Sprint 11 - Session Management avec @Session</p>
        
        <div class="info-box">
            <strong>ℹ️ Fonctionnement:</strong><br>
            - La session est stockée via <code style="background: #fff; padding: 2px 6px;">@Session Map&lt;String, Object&gt;</code><br>
            - Quand vous quittez et revenez, vos articles restent (session persistente)<br>
            - Actualisez pour voir la session en action!
        </div>
        
        <!-- Message condition -->
        <c:if test="${not empty nombreArticles && nombreArticles > 0}">
            <div class="panier-stats">
                <div class="stat">
                    <div class="stat-number">${nombreArticles}</div>
                    <div class="stat-label">Article(s)</div>
                </div>
                <div class="stat">
                    <div class="stat-label">Dernier ajout</div>
                    <div style="font-size: 18px; font-weight: bold; color: #333; margin-top: 5px;">${dernier}</div>
                </div>
            </div>
        </c:if>
        
        <c:if test="${not empty message}">
            <div class="message ${empty panier ? 'empty' : ''}">
                ${message}
            </div>
        </c:if>
        
        <!-- Affichage du panier -->
        <div class="articles">
            <c:if test="${not empty panier && panier.size() > 0}">
                <c:forEach items="${panier}" var="article">
                    <div class="article-item">
                        <div class="article-content">
                            <div class="article-name">${article}</div>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
            
            <c:if test="${empty panier || panier.size() == 0}">
                <div class="empty-panier">
                    <div class="empty-panier-icon">📦</div>
                    <h3>Panier vide</h3>
                    <p style="margin-top: 10px; color: #999;">Commencez à ajouter des produits!</p>
                </div>
            </c:if>
        </div>
        
        <!-- Boutons d'action -->
        <div class="button-group">
            <a href="form" class="btn btn-primary">Ajouter un produit</a>
            <c:if test="${not empty panier && panier.size() > 0}">
                <a href="vider" class="btn btn-danger">Vider le panier</a>
            </c:if>
        </div>
    </div>
</body>
</html>
