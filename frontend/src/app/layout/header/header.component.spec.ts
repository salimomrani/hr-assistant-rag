import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have menu items for Chat and Admin', () => {
    expect(component.menuItems).toHaveLength(2);
    expect(component.menuItems[0].label).toBe('Chat');
    expect(component.menuItems[1].label).toBe('Admin');
  });

  it('should display brand title', () => {
    const compiled = fixture.nativeElement;
    const brandTitle = compiled.querySelector('.brand-title');
    expect(brandTitle.textContent).toContain('HR Assistant');
  });

  it('should display brand subtitle', () => {
    const compiled = fixture.nativeElement;
    const brandSubtitle = compiled.querySelector('.brand-subtitle');
    expect(brandSubtitle.textContent).toContain('Intelligent Document Intelligence');
  });

  it('should have navigation links', () => {
    const compiled = fixture.nativeElement;
    const navLinks = compiled.querySelectorAll('.nav-link');
    expect(navLinks.length).toBe(2);
  });

  it('should have Chat navigation link with correct route', () => {
    const compiled = fixture.nativeElement;
    const chatLink = compiled.querySelector('a[routerLink="/chat"]');
    expect(chatLink).toBeTruthy();
    expect(chatLink.textContent).toContain('Chat');
  });

  it('should have Admin navigation link with correct route', () => {
    const compiled = fixture.nativeElement;
    const adminLink = compiled.querySelector('a[routerLink="/admin"]');
    expect(adminLink).toBeTruthy();
    expect(adminLink.textContent).toContain('Admin');
  });

  it('should apply active class to current route', async () => {
    const compiled = fixture.nativeElement;
    await router.navigate(['/chat']);
    fixture.detectChanges();

    const chatLink = compiled.querySelector('a[routerLink="/chat"]');
    expect(chatLink.classList.contains('nav-link-active')).toBeTruthy();
  });

  it('should have brand icon SVG', () => {
    const compiled = fixture.nativeElement;
    const brandIcon = compiled.querySelector('.brand-icon svg');
    expect(brandIcon).toBeTruthy();
  });

  it('should have navigation icons', () => {
    const compiled = fixture.nativeElement;
    const icons = compiled.querySelectorAll('.nav-link i');
    expect(icons.length).toBe(2);
    expect(icons[0].classList.contains('pi-comments')).toBeTruthy();
    expect(icons[1].classList.contains('pi-cog')).toBeTruthy();
  });
});
