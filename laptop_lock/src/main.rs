use axum::{
    routing::post, 
    Router, 
    http::{StatusCode, Request}, 
    response::IntoResponse, 
    body::Body
};
use std::process::Command;
use std::env;
use tower_http::validate_request::ValidateRequestHeaderLayer;
use dotenvy::dotenv;

#[tokio::main]
async fn main() {
    // Load .env file
    dotenv().expect(".env file not found");

    let secure_key = env::var("API_KEY").expect("API_KEY not set in .env");
    let port = env::var("PORT").unwrap_or_else(|_| "8080".to_string());
    let bind_addr = format!("0.0.0.0:{}", port);

    let app = Router::new()
        .route("/lock", post(lock_handler))
        .layer(ValidateRequestHeaderLayer::custom(move |request: &mut Request<Body>| {
            let api_key = request
                .headers()
                .get("X-Api-Key")
                .and_then(|value| value.to_str().ok());

            if api_key == Some(&secure_key) {
                Ok(())
            } else {
                Err(StatusCode::UNAUTHORIZED.into_response())
            }
        }));

    let listener = tokio::net::TcpListener::bind(&bind_addr).await.unwrap();
    println!("Aegis Server listening on {}...", bind_addr);
    axum::serve(listener, app).await.unwrap();
}

async fn lock_handler() -> impl IntoResponse {
    #[cfg(target_os = "linux")]
    {
        let _ = Command::new("hyprlock").spawn();
    }

    StatusCode::OK
}
