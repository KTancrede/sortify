package com.tancrede.spotifytinder.spotify;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import fi.iki.elonen.NanoHTTPD;

import java.net.URI;

public class SpotifyAuthManager {

    private static final String clientID = "09760f1edaf4414a93f6579d655a0d44";
    private static final String clientSecret = "d0a3340ecc7046c8969a373759cc4474";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://127.0.0.1:3000");

    private SpotifyApi spotifyApi;

    public SpotifyAuthManager() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientID)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
    }

    public void authorizeUser() {
        AuthorizationCodeUriRequest uriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-library-read playlist-read-private playlist-modify-private playlist-modify-public user-library-modify")
                .show_dialog(true)
                .build();

        URI uri = uriRequest.execute();
        System.out.println("👉 Ouverture du navigateur : " + uri);

        try {
            // Ouvre le navigateur
            java.awt.Desktop.getDesktop().browse(uri);

            // Lance le serveur local
            CodeReceiverServer server = new CodeReceiverServer(3000);
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

            System.out.println("⏳ En attente du code d'autorisation...");
            while (server.getCode() == null) {
                Thread.sleep(500);
            }

            String code = server.getCode();
            server.stop();

            AuthorizationCodeRequest authRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials credentials = authRequest.execute();

            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            System.out.println("✅ Connexion réussie à Spotify !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    // Mini serveur HTTP pour capturer le code
    private static class CodeReceiverServer extends NanoHTTPD {
        private String code;

        public CodeReceiverServer(int port) {
            super(port);
        }

        public String getCode() {
            return code;
        }

        @Override
        public Response serve(IHTTPSession session) {
            String query = session.getQueryParameterString();
            if (query != null && query.contains("code=")) {
                this.code = query.split("code=")[1].split("&")[0];

                // HTML avec fermeture automatique
                String html = """
                    <html>
                      <head>
                        <title>Connexion réussie</title>
                        <script>
                          setTimeout(function() {
                            window.close();
                          }, 1500);
                        </script>
                      </head>
                      <body style="font-family: sans-serif; text-align: center; margin-top: 50px;">
                        <h2>✅ Connexion réussie !</h2>
                        <p>Tu peux retourner sur l'application. Cette fenêtre va se fermer automatiquement.</p>
                      </body>
                    </html>
                """;

                return newFixedLengthResponse(Response.Status.OK, "text/html", html);
            } else {
                return newFixedLengthResponse("Erreur : aucun code reçu.");
            }
        }

    }
}
