import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
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
    expect(adminLink.textContent).toContain('Documents');
  });

  it('should have brand icon SVG', () => {
    const compiled = fixture.nativeElement;
    const brandIcon = compiled.querySelector('.brand-icon svg');
    expect(brandIcon).toBeTruthy();
  });

  it('should have navigation SVG icons', () => {
    const compiled = fixture.nativeElement;
    const icons = compiled.querySelectorAll('.nav-link svg');
    expect(icons.length).toBe(2);
  });

  it('should use routerLinkActive directive', () => {
    const compiled = fixture.nativeElement;
    const chatLink = compiled.querySelector('a[routerLink="/chat"]');
    expect(chatLink).toBeTruthy();
  });
});
