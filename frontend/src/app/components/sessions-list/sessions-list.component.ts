import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SessionSummary } from '../../models/session-summary.model';
import { EchoApiService } from '../../services/echo-api.service';

/**
 * Sessions List Component
 * Showcases: OnInit lifecycle, async data handling, Material UI, routing
 */
@Component({
  selector: 'app-sessions-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="sessions-container">
      <h1>Recording Sessions</h1>

      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading sessions...</p>
      </div>

      <div *ngIf="!loading && error" class="error-container">
        <mat-icon color="warn">error</mat-icon>
        <p>{{ error }}</p>
        <button mat-raised-button color="primary" (click)="loadSessions()">
          <mat-icon>refresh</mat-icon>
          Retry
        </button>
      </div>

      <div *ngIf="!loading && !error" class="sessions-grid">
        <mat-card *ngFor="let session of sessions" class="session-card" (click)="viewSession(session.sessionId)">
          <mat-card-header>
            <mat-icon mat-card-avatar>folder</mat-icon>
            <mat-card-title>{{ session.sessionId }}</mat-card-title>
            <mat-card-subtitle>{{ session.recordCount }} recorded requests</mat-card-subtitle>
          </mat-card-header>
          <mat-card-actions>
            <button mat-button color="primary">
              <mat-icon>visibility</mat-icon>
              View Traffic
            </button>
          </mat-card-actions>
        </mat-card>

        <div *ngIf="sessions.length === 0" class="empty-state">
          <mat-icon>inbox</mat-icon>
          <p>No sessions found</p>
          <p class="subtitle">Record some traffic to see sessions here</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .sessions-container {
      padding: 16px;
    }

    h1 {
      margin: 0 0 24px 0;
      color: #3f51b5;
    }

    .loading-container,
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      text-align: center;
    }

    .loading-container mat-spinner {
      margin-bottom: 16px;
    }

    .error-container mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    .sessions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;
    }

    .session-card {
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .session-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.2);
    }

    .session-card mat-icon[mat-card-avatar] {
      font-size: 40px;
      width: 40px;
      height: 40px;
    }

    .empty-state {
      grid-column: 1 / -1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px;
      text-align: center;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    .empty-state .subtitle {
      font-size: 14px;
      margin-top: 8px;
    }
  `]
})
export class SessionsListComponent implements OnInit {
  sessions: SessionSummary[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private echoApiService: EchoApiService,
    private router: Router
  ) {}

  /**
   * Lifecycle hook - called after component initialization
   * Showcases: Angular lifecycle hooks, automatic method invocation
   */
  ngOnInit(): void {
    this.loadSessions();
  }

  /**
   * Load sessions from API
   * Showcases: Observable subscription, error handling, state management
   */
  loadSessions(): void {
    this.loading = true;
    this.error = null;

    this.echoApiService.getSessions().subscribe({
      next: (data) => {
        this.sessions = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
        console.error('Failed to load sessions:', err);
      }
    });
  }

  /**
   * Navigate to session detail view
   * Showcases: Programmatic routing
   */
  viewSession(sessionId: string): void {
    this.router.navigate(['/sessions', sessionId]);
  }
}
