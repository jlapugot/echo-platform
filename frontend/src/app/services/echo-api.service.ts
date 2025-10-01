import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { SessionSummary } from '../models/session-summary.model';
import { TrafficRecord } from '../models/traffic-record.model';

@Injectable({
  providedIn: 'root'
})
export class EchoApiService {
  private readonly API_URL = 'http://localhost:8082/api/v1';

  constructor(private http: HttpClient) { }

  /**
   * Fetch all sessions with their record counts
   * Showcases: HttpClient GET, Observable typing, RxJS operators
   */
  getSessions(): Observable<SessionSummary[]> {
    return this.http.get<SessionSummary[]>(`${this.API_URL}/sessions`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Fetch all traffic records for a specific session
   * Showcases: Path parameters, error handling
   */
  getTrafficBySession(sessionId: string): Observable<TrafficRecord[]> {
    return this.http.get<TrafficRecord[]>(`${this.API_URL}/sessions/${sessionId}/traffic`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Delete a specific traffic record by ID
   * Showcases: HTTP DELETE, RESTful API operations
   */
  deleteTrafficRecord(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/traffic/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Delete all traffic records for a session
   * Showcases: Bulk delete operations
   */
  deleteSessionTraffic(sessionId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/sessions/${sessionId}/traffic`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Error handling utility
   * Showcases: RxJS error operators, proper error handling patterns
   */
  private handleError(error: any): Observable<never> {
    console.error('API Error:', error);
    let errorMessage = 'An error occurred while fetching data';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    return throwError(() => new Error(errorMessage));
  }
}
