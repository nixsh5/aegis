use axum::{
    routing::post,
    Router,
    http::{StatusCode, Request},
    response::IntoResponse,
    body::Body
};
use std::process::Command;
use tower_http::validate_request::ValidateRequestHeaderLayer;

#[tokio::main]
async fn main() {
    let app = Router::new()
        .route("/lock", post(lock_handler))
        .layer(ValidateRequestHeaderLayer::custom(|request: &mut Request<Body>| {
            let api_key = request
                .headers()
                .get("X Api Key")
                .and_then(|value| value.to_str().ok());

            if api_key == Some("MY_SUPER_SECRET_API_KEY") {
                Ok(())
            } else {
                Err(StatusCode::UNAUTHORIZED.into_response())
            }
        }));

    let listener = tokio::net::TcpListener::bind("0.0.0.0:8080").await.unwrap();
    println!("Server listening on port 8080...");
    axum::serve(listener, app).await.unwrap();
}

async fn lock_handler() -> impl IntoResponse {
    #[cfg(target_os = "linux")]
    let _ = Command::new("hyprlock").arg("lock").spawn();

    StatusCode::OK
}
