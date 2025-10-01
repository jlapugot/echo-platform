import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TrafficRecord } from '../../models/traffic-record.model';
import { EchoApiService } from '../../services/echo-api.service';

/**
 * Traffic Detail Component
 * Showcases: Route parameters, expansion panels, advanced data display
 */
@Component({
  selector: 'app-traffic-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTableModule
  ],
  template: `
    <div class="traffic-container">
      <div class="header">
        <button mat-icon-button routerLink="/sessions">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>Session: {{ sessionId }}</h1>
      </div>

      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading traffic records...</p>
      </div>

      <div *ngIf="!loading && error" class="error-container">
        <mat-icon color="warn">error</mat-icon>
        <p>{{ error }}</p>
      </div>

      <div *ngIf="!loading && !error" class="traffic-list">
        <mat-accordion *ngIf="trafficRecords.length > 0">
          <mat-expansion-panel *ngFor="let record of trafficRecords; let i = index">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-chip [class]="'method-chip method-' + record.method.toLowerCase()">
                  {{ record.method }}
                </mat-chip>
                <span class="path">{{ record.path }}</span>
              </mat-panel-title>
              <mat-panel-description>
                <mat-chip [class]="'status-chip status-' + getStatusClass(record.statusCode)">
                  {{ record.statusCode }}
                </mat-chip>
                <span class="timestamp">{{ formatTimestamp(record.timestamp) }}</span>
              </mat-panel-description>
            </mat-expansion-panel-header>

            <div class="record-details">
              <!-- Request Section -->
              <div class="section">
                <h3><mat-icon>arrow_upward</mat-icon> Request</h3>

                <div class="subsection">
                  <h4>Headers</h4>
                  <div class="headers">
                    <div *ngFor="let header of objectToArray(record.requestHeaders)" class="header-row">
                      <span class="header-key">{{ header.key }}:</span>
                      <span class="header-value">{{ header.value }}</span>
                    </div>
                  </div>
                </div>

                <div class="subsection" *ngIf="record.requestBody">
                  <h4>Body</h4>
                  <pre><code>{{ formatJson(record.requestBody) }}</code></pre>
                </div>
              </div>

              <!-- Response Section -->
              <div class="section">
                <h3><mat-icon>arrow_downward</mat-icon> Response</h3>

                <div class="subsection">
                  <h4>Headers</h4>
                  <div class="headers">
                    <div *ngFor="let header of objectToArray(record.responseHeaders)" class="header-row">
                      <span class="header-key">{{ header.key }}:</span>
                      <span class="header-value">{{ header.value }}</span>
                    </div>
                  </div>
                </div>

                <div class="subsection" *ngIf="record.responseBody">
                  <h4>Body</h4>
                  <pre><code>{{ formatJson(record.responseBody) }}</code></pre>
                </div>
              </div>

              <!-- Metadata -->
              <div class="metadata">
                <p><strong>Recorded:</strong> {{ formatTimestamp(record.createdAt) }}</p>
                <p><strong>ID:</strong> {{ record.id }}</p>
              </div>
            </div>
          </mat-expansion-panel>
        </mat-accordion>

        <div *ngIf="trafficRecords.length === 0" class="empty-state">
          <mat-icon>inbox</mat-icon>
          <p>No traffic records found for this session</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .traffic-container {
      padding: 16px;
    }

    .header {
      display: flex;
      align-items: center;
      margin-bottom: 24px;
    }

    .header h1 {
      margin: 0 0 0 8px;
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

    mat-expansion-panel {
      margin-bottom: 8px;
    }

    mat-panel-title {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    mat-panel-description {
      display: flex;
      align-items: center;
      gap: 12px;
      justify-content: flex-end;
    }

    .method-chip {
      font-weight: 600;
      min-width: 60px;
      text-align: center;
    }

    .method-get { background-color: #4caf50 !important; color: white !important; }
    .method-post { background-color: #2196f3 !important; color: white !important; }
    .method-put { background-color: #ff9800 !important; color: white !important; }
    .method-delete { background-color: #f44336 !important; color: white !important; }
    .method-patch { background-color: #9c27b0 !important; color: white !important; }

    .status-chip {
      font-weight: 600;
      min-width: 50px;
      text-align: center;
    }

    .status-success { background-color: #4caf50 !important; color: white !important; }
    .status-redirect { background-color: #2196f3 !important; color: white !important; }
    .status-client-error { background-color: #ff9800 !important; color: white !important; }
    .status-server-error { background-color: #f44336 !important; color: white !important; }

    .path {
      font-family: 'Courier New', monospace;
      flex: 1;
    }

    .timestamp {
      font-size: 12px;
      color: #666;
    }

    .record-details {
      padding: 16px 0;
    }

    .section {
      margin-bottom: 24px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 16px 0;
      color: #3f51b5;
    }

    .subsection {
      margin-bottom: 16px;
    }

    .subsection h4 {
      margin: 0 0 8px 0;
      color: #666;
      font-size: 14px;
      text-transform: uppercase;
    }

    .headers {
      font-family: 'Courier New', monospace;
      font-size: 13px;
    }

    .header-row {
      display: flex;
      gap: 8px;
      padding: 4px 0;
    }

    .header-key {
      font-weight: 600;
      color: #3f51b5;
    }

    .header-value {
      color: #666;
    }

    pre {
      background-color: #263238;
      color: #aed581;
      padding: 16px;
      border-radius: 4px;
      overflow-x: auto;
      margin: 0;
    }

    .metadata {
      padding: 16px;
      background-color: #e3f2fd;
      border-radius: 4px;
      font-size: 13px;
    }

    .metadata p {
      margin: 4px 0;
    }

    .empty-state {
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
  `]
})
export class TrafficDetailComponent implements OnInit {
  sessionId: string = '';
  trafficRecords: TrafficRecord[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private echoApiService: EchoApiService
  ) {}

  /**
   * Initialize component and load traffic data
   * Showcases: Route parameter extraction
   */
  ngOnInit(): void {
    this.sessionId = this.route.snapshot.paramMap.get('id') || '';
    this.loadTraffic();
  }

  /**
   * Load traffic records for the session
   */
  loadTraffic(): void {
    this.loading = true;
    this.error = null;

    this.echoApiService.getTrafficBySession(this.sessionId).subscribe({
      next: (data) => {
        this.trafficRecords = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  /**
   * Utility: Convert object to array for *ngFor
   * Showcases: Data transformation for templates
   */
  objectToArray(obj: { [key: string]: string }): { key: string; value: string }[] {
    return Object.entries(obj).map(([key, value]) => ({ key, value }));
  }

  /**
   * Utility: Format JSON string for display
   * Showcases: String manipulation, error handling
   */
  formatJson(str: string): string {
    try {
      return JSON.stringify(JSON.parse(str), null, 2);
    } catch {
      return str;
    }
  }

  /**
   * Utility: Format timestamp to readable date
   * Showcases: Date formatting
   */
  formatTimestamp(timestamp: string): string {
    return new Date(timestamp).toLocaleString();
  }

  /**
   * Utility: Get CSS class for status code
   * Showcases: Conditional logic for styling
   */
  getStatusClass(statusCode: number): string {
    if (statusCode >= 200 && statusCode < 300) return 'success';
    if (statusCode >= 300 && statusCode < 400) return 'redirect';
    if (statusCode >= 400 && statusCode < 500) return 'client-error';
    return 'server-error';
  }
}
