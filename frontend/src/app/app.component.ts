import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

/**
 * Root application component
 * Showcases: Standalone components, Material UI integration
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <mat-toolbar color="primary">
      <mat-icon>cloud_queue</mat-icon>
      <span class="toolbar-title">Echo Platform</span>
      <span class="spacer"></span>
      <button mat-button routerLink="/try-it">
        <mat-icon>play_circle</mat-icon>
        Try It
      </button>
      <button mat-button routerLink="/sessions">
        <mat-icon>list</mat-icon>
        Sessions
      </button>
    </mat-toolbar>

    <div class="content">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .toolbar-title {
      margin-left: 16px;
      font-size: 20px;
      font-weight: 500;
    }

    .spacer {
      flex: 1 1 auto;
    }

    .content {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }
  `]
})
export class AppComponent {
  title = 'Echo Platform Dashboard';
}
