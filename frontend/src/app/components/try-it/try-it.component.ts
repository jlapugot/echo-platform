import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ProxyService, ProxyRequest } from '../../services/proxy.service';
import { EchoApiService } from '../../services/echo-api.service';

/**
 * Try It Component - Interactive demo page
 * Showcases: Reactive forms, HTTP requests, real-time updates
 */
@Component({
  selector: 'app-try-it',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressBarModule,
    MatDividerModule,
    MatSlideToggleModule
  ],
  template: `
    <div class="try-it-container">
      <div class="header">
        <h1>Try Echo Platform</h1>
        <p class="subtitle">Send requests through the proxy and watch them being recorded!</p>
      </div>

      <div class="content-grid">
        <!-- Request Builder -->
        <mat-card class="request-builder">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>send</mat-icon>
              Request Builder
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-section">
              <mat-form-field class="full-width">
                <mat-label>HTTP Method</mat-label>
                <mat-select [(ngModel)]="request.method">
                  <mat-option value="GET">GET</mat-option>
                  <mat-option value="POST">POST</mat-option>
                  <mat-option value="PUT">PUT</mat-option>
                  <mat-option value="DELETE">DELETE</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field class="full-width">
                <mat-label>Target URL</mat-label>
                <input matInput [(ngModel)]="request.url" placeholder="https://jsonplaceholder.typicode.com/users/1">
                <mat-icon matSuffix>link</mat-icon>
              </mat-form-field>

              <mat-form-field class="full-width">
                <mat-label>Authorization Header (Optional)</mat-label>
                <input matInput [(ngModel)]="authHeader" placeholder="Bearer your-token-here">
                <mat-icon matSuffix>lock</mat-icon>
                <mat-hint>Add authentication token for testing protected endpoints</mat-hint>
              </mat-form-field>

              <div class="quick-examples">
                <span class="label">Quick examples:</span>
                <button mat-stroked-button size="small" (click)="loadExample('users')">
                  <mat-icon>person</mat-icon>
                  Get User
                </button>
                <button mat-stroked-button size="small" (click)="loadExample('posts')">
                  <mat-icon>article</mat-icon>
                  Get Posts
                </button>
                <button mat-stroked-button size="small" (click)="loadExample('create')">
                  <mat-icon>add</mat-icon>
                  Create Post
                </button>
              </div>

              <mat-form-field class="full-width" *ngIf="request.method === 'POST' || request.method === 'PUT'">
                <mat-label>Request Body (JSON)</mat-label>
                <textarea matInput [(ngModel)]="request.body" rows="6" placeholder='{"title": "Test", "body": "Content"}'></textarea>
              </mat-form-field>

              <button mat-raised-button color="primary" (click)="sendRequest()" [disabled]="loading" class="send-button">
                <mat-icon>play_arrow</mat-icon>
                Send Request via Proxy
              </button>

              <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Mode Indicator -->
        <mat-card class="mode-indicator">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>settings</mat-icon>
              Proxy Status
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="mode-display">
              <div class="mode-switch">
                <span class="mode-label">RECORD</span>
                <mat-slide-toggle
                  [checked]="currentMode === 'REPLAY'"
                  (change)="toggleMode($event.checked)"
                  [disabled]="switchingMode"
                  color="primary">
                </mat-slide-toggle>
                <span class="mode-label">REPLAY</span>
              </div>

              <mat-chip [class]="'mode-chip mode-' + currentMode.toLowerCase()">
                <mat-icon>{{getCurrentModeIcon()}}</mat-icon>
                {{currentMode}} MODE
              </mat-chip>
              <p class="mode-description">
                {{ getModeDescription() }}
              </p>
            </div>

            <mat-divider></mat-divider>

            <div class="stats" *ngIf="sessionStats">
              <h4>Current Session: default-session</h4>
              <div class="stat-item">
                <mat-icon>storage</mat-icon>
                <span>{{ sessionStats.recordCount }} requests recorded</span>
              </div>
              <div class="button-group">
                <button mat-button color="primary" routerLink="/sessions/default-session">
                  <mat-icon>visibility</mat-icon>
                  View Traffic
                </button>
                <button mat-button color="warn" (click)="clearSession()" [disabled]="!sessionStats || sessionStats.recordCount === 0">
                  <mat-icon>delete_sweep</mat-icon>
                  Clear Session
                </button>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Response Display -->
        <mat-card class="response-display" *ngIf="response">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>receipt</mat-icon>
              Response
              <mat-chip [class]="'status-chip status-' + getStatusClass(response.status)">
                {{ response.status }} {{ response.statusText }}
              </mat-chip>
              <mat-chip *ngIf="authHeader" class="auth-chip" color="accent">
                <mat-icon>lock</mat-icon>
                Authenticated
              </mat-chip>
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="response-info">
              <span><mat-icon>schedule</mat-icon> Duration: {{ response.duration }}ms</span>
            </div>

            <h4>Response Body</h4>
            <pre class="response-body"><code>{{ formatJson(response.body) }}</code></pre>

            <div class="success-message" *ngIf="response.status >= 200 && response.status < 300">
              <mat-icon>check_circle</mat-icon>
              <span *ngIf="currentMode === 'RECORD'">Request successful! This traffic has been recorded and can be replayed later.</span>
              <span *ngIf="currentMode === 'REPLAY'">Request successful! This response was served from cache (replay mode).</span>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Error Display -->
        <mat-card class="error-display" *ngIf="error">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="warn">error</mat-icon>
              Error
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>{{ error }}</p>
          </mat-card-content>
        </mat-card>

        <!-- How It Works -->
        <mat-card class="how-it-works">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>lightbulb</mat-icon>
              How It Works
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="workflow">
              <div class="step">
                <div class="step-number">1</div>
                <div class="step-content">
                  <h4>Send Request</h4>
                  <p>Your request goes through the Echo Proxy (port 8080)</p>
                </div>
              </div>
              <mat-icon class="arrow">arrow_downward</mat-icon>
              <div class="step">
                <div class="step-number">2</div>
                <div class="step-content">
                  <h4>Forward & Record</h4>
                  <p>Proxy forwards to target API and records the traffic</p>
                </div>
              </div>
              <mat-icon class="arrow">arrow_downward</mat-icon>
              <div class="step">
                <div class="step-number">3</div>
                <div class="step-content">
                  <h4>Save to Database</h4>
                  <p>Request/response saved via RabbitMQ → Ingestor → PostgreSQL</p>
                </div>
              </div>
              <mat-icon class="arrow">arrow_downward</mat-icon>
              <div class="step">
                <div class="step-number">4</div>
                <div class="step-content">
                  <h4>View & Replay</h4>
                  <p>Later, switch to REPLAY mode to serve cached responses</p>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .try-it-container {
      padding: 16px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      margin-bottom: 24px;
    }

    .header h1 {
      margin: 0 0 8px 0;
      color: #3f51b5;
    }

    .subtitle {
      margin: 0;
      color: #666;
      font-size: 16px;
    }

    .content-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    .request-builder {
      grid-column: 1 / 2;
    }

    .mode-indicator {
      grid-column: 2 / 3;
    }

    .response-display,
    .error-display {
      grid-column: 1 / 2;
    }

    .how-it-works {
      grid-column: 2 / 3;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .form-section {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .quick-examples {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .quick-examples .label {
      font-size: 14px;
      color: #666;
      margin-right: 8px;
    }

    .quick-examples button {
      font-size: 12px;
    }

    .send-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
    }

    .mode-display {
      text-align: center;
      padding: 16px 0;
    }

    .mode-switch {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-bottom: 16px;
    }

    .mode-label {
      font-weight: 600;
      color: #666;
      font-size: 14px;
    }

    .mode-chip {
      font-size: 16px;
      font-weight: 600;
      padding: 8px 16px;
    }

    .mode-chip.mode-record {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .mode-chip.mode-replay {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .mode-description {
      margin-top: 12px;
      color: #666;
      font-size: 14px;
    }

    .stats {
      padding: 16px 0;
    }

    .stats h4 {
      margin: 0 0 12px 0;
      color: #3f51b5;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      color: #666;
    }

    .button-group {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .button-group button {
      flex: 1;
    }

    .response-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      padding: 8px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .response-body {
      background-color: #263238;
      color: #aed581;
      padding: 16px;
      border-radius: 4px;
      overflow-x: auto;
      overflow-y: auto;
      margin: 0 0 16px 0;
      max-height: 400px;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .success-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background-color: #e8f5e9;
      color: #2e7d32;
      border-radius: 4px;
      border-left: 4px solid #4caf50;
    }

    .success-message mat-icon {
      color: #4caf50;
    }

    .status-chip {
      margin-left: 8px;
      font-weight: 600;
    }

    .auth-chip {
      margin-left: 8px;
      font-weight: 500;
    }

    .auth-chip mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }

    .status-success { background-color: #4caf50 !important; color: white !important; }
    .status-redirect { background-color: #2196f3 !important; color: white !important; }
    .status-client-error { background-color: #ff9800 !important; color: white !important; }
    .status-server-error { background-color: #f44336 !important; color: white !important; }

    .workflow {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .step {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .step-number {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background-color: #3f51b5;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      flex-shrink: 0;
    }

    .step-content h4 {
      margin: 0 0 4px 0;
      color: #3f51b5;
    }

    .step-content p {
      margin: 0;
      font-size: 13px;
      color: #666;
    }

    .arrow {
      color: #3f51b5;
      margin: 0 auto;
    }

    @media (max-width: 960px) {
      .content-grid {
        grid-template-columns: 1fr;
      }

      .request-builder,
      .mode-indicator,
      .response-display,
      .error-display,
      .how-it-works {
        grid-column: 1 / 2;
      }
    }
  `]
})
export class TryItComponent implements OnInit {
  request: ProxyRequest = {
    url: 'https://jsonplaceholder.typicode.com/users/1',
    method: 'GET',
    body: ''
  };

  authHeader: string = '';
  response: any = null;
  error: string | null = null;
  loading = false;
  sessionStats: any = null;
  currentMode: 'RECORD' | 'REPLAY' = 'RECORD';
  switchingMode = false;

  constructor(
    private proxyService: ProxyService,
    private echoApiService: EchoApiService
  ) {}

  ngOnInit(): void {
    this.loadCurrentMode();
    this.loadSessionStats();
  }

  loadCurrentMode(): void {
    this.proxyService.getMode().subscribe({
      next: (response) => {
        this.currentMode = response.mode as 'RECORD' | 'REPLAY';
      },
      error: (err) => console.error('Failed to load current mode:', err)
    });
  }

  toggleMode(isReplay: boolean): void {
    const newMode = isReplay ? 'REPLAY' : 'RECORD';
    this.switchingMode = true;

    this.proxyService.switchMode(newMode).subscribe({
      next: (response) => {
        this.currentMode = response.mode as 'RECORD' | 'REPLAY';
        this.switchingMode = false;
        console.log(`Switched to ${this.currentMode} mode`);
      },
      error: (err) => {
        console.error('Failed to switch mode:', err);
        this.switchingMode = false;
      }
    });
  }

  loadSessionStats(): void {
    this.echoApiService.getSessions().subscribe({
      next: (sessions) => {
        this.sessionStats = sessions.find(s => s.sessionId === 'default-session');
      },
      error: (err) => console.error('Failed to load session stats:', err)
    });
  }

  sendRequest(): void {
    this.loading = true;
    this.response = null;
    this.error = null;

    const startTime = Date.now();

    // Add auth header if provided
    const requestWithHeaders = { ...this.request };
    if (this.authHeader) {
      requestWithHeaders.headers = {
        ...requestWithHeaders.headers,
        'Authorization': this.authHeader
      };
    }

    this.proxyService.sendRequest(requestWithHeaders).subscribe({
      next: (httpResponse) => {
        const duration = Date.now() - startTime;

        this.response = {
          status: httpResponse.status,
          statusText: httpResponse.statusText,
          headers: this.extractHeaders(httpResponse.headers),
          body: httpResponse.body,
          duration
        };

        this.loading = false;

        // Reload stats after a short delay to allow recording to complete
        // Only reload in RECORD mode since REPLAY doesn't create new records
        if (this.currentMode === 'RECORD') {
          setTimeout(() => this.loadSessionStats(), 2000);
        }
      },
      error: (err) => {
        this.error = `Failed to send request: ${err.message}`;
        this.loading = false;
      }
    });
  }

  loadExample(type: string): void {
    switch (type) {
      case 'users':
        this.request = {
          url: 'https://jsonplaceholder.typicode.com/users/1',
          method: 'GET'
        };
        break;
      case 'posts':
        this.request = {
          url: 'https://jsonplaceholder.typicode.com/posts',
          method: 'GET'
        };
        break;
      case 'create':
        this.request = {
          url: 'https://jsonplaceholder.typicode.com/posts',
          method: 'POST',
          body: JSON.stringify({
            title: 'Test Post from Echo Platform',
            body: 'This is a demo request to show how Echo records traffic',
            userId: 1
          }, null, 2)
        };
        break;
    }
  }

  formatJson(str: string): string {
    try {
      return JSON.stringify(JSON.parse(str), null, 2);
    } catch {
      return str;
    }
  }

  extractHeaders(headers: any): { [key: string]: string } {
    const result: { [key: string]: string } = {};
    headers.keys().forEach((key: string) => {
      result[key] = headers.get(key);
    });
    return result;
  }

  getStatusClass(statusCode: number): string {
    if (statusCode >= 200 && statusCode < 300) return 'success';
    if (statusCode >= 300 && statusCode < 400) return 'redirect';
    if (statusCode >= 400 && statusCode < 500) return 'client-error';
    return 'server-error';
  }

  getCurrentModeIcon(): string {
    return this.currentMode === 'RECORD' ? 'fiber_manual_record' : 'replay';
  }

  getModeDescription(): string {
    return this.currentMode === 'RECORD'
      ? 'All requests are being forwarded to the target and recorded to the database.'
      : 'Requests are being served from previously recorded responses in the database.';
  }

  clearSession(): void {
    if (!confirm('Are you sure you want to delete all recorded traffic for this session? This cannot be undone.')) {
      return;
    }

    this.echoApiService.deleteSessionTraffic('default-session').subscribe({
      next: () => {
        console.log('Session cleared successfully');
        this.loadSessionStats();
      },
      error: (err) => {
        console.error('Failed to clear session:', err);
        this.error = 'Failed to clear session';
      }
    });
  }
}
