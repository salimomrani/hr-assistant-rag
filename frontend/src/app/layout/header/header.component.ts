import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem } from 'primeng/api';

/**
 * Header Component - Application navigation header
 * Features refined professional design with PrimeNG menubar
 */
@Component({
  selector: 'app-header',
  imports: [MenubarModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private router = inject(Router);

  menuItems: MenuItem[] = [
    {
      label: 'Chat',
      icon: 'pi pi-comments',
      routerLink: '/chat',
      command: () => this.router.navigate(['/chat'])
    },
    {
      label: 'Admin',
      icon: 'pi pi-cog',
      routerLink: '/admin',
      command: () => this.router.navigate(['/admin'])
    }
  ];
}
