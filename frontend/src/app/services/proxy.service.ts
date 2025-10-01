import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProxyRequest {
  url: string;
  method: string;
  headers?: { [key: string]: string };
  body?: string;
}

export interface ProxyResponse {
  status: number;
  statusText: string;
  headers: { [key: string]: string };
  body: any;
  duration: number;
}

export interface ModeResponse {
  mode: string;
  sessionId?: string;
  message?: string;
}

/**
 * Service to send requests through Echo Proxy
 * Showcases: HTTP client usage, proxy pattern
 */
@Injectable({
  providedIn: 'root'
})
export class ProxyService {
  private readonly PROXY_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /**
   * Send a request through the Echo Proxy
   * This demonstrates the proxy intercepting and recording/replaying traffic
   */
  sendRequest(request: ProxyRequest): Observable<HttpResponse<any>> {
    const startTime = Date.now();

    // Build headers - only set Content-Type for POST/PUT with body
    let headers = new HttpHeaders();

    if ((request.method === 'POST' || request.method === 'PUT') && request.body) {
      headers = headers.set('Content-Type', 'application/json');
    }

    if (request.headers) {
      Object.keys(request.headers).forEach(key => {
        headers = headers.set(key, request.headers![key]);
      });
    }

    // Parse the target URL path from full URL
    const urlPath = request.url.replace(/^https?:\/\/[^\/]+/, '');

    // Send through proxy
    const options = {
      headers,
      observe: 'response' as const,
      responseType: 'text' as const
    };

    const proxyUrl = `${this.PROXY_URL}${urlPath}`;

    switch (request.method.toUpperCase()) {
      case 'POST':
        return this.http.post(proxyUrl, request.body, options);
      case 'PUT':
        return this.http.put(proxyUrl, request.body, options);
      case 'DELETE':
        return this.http.delete(proxyUrl, options);
      default:
        return this.http.get(proxyUrl, options);
    }
  }

  /**
   * Get current proxy mode
   */
  getMode(): Observable<ModeResponse> {
    return this.http.get<ModeResponse>(`${this.PROXY_URL}/api/mode`);
  }

  /**
   * Switch proxy mode between RECORD and REPLAY
   */
  switchMode(mode: 'RECORD' | 'REPLAY'): Observable<ModeResponse> {
    return this.http.post<ModeResponse>(`${this.PROXY_URL}/api/mode`, { mode });
  }
}
