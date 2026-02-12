import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  rol: string;
  success: boolean;
  message: string;
}

export interface User {
  id: number;
  username: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeUser();
    this.setupStorageListener();
  }

  private setupStorageListener(): void {
    window.addEventListener('storage', (event) => {
      if (event.key === 'token' && event.newValue !== event.oldValue) {
        this.initializeUser();
      }
    });
  }

  private initializeUser(): void {
    const token = this.getToken();
    if (token) {
      try {
        if (!this.isAuthenticated()) {
          console.warn('Token expirado, limpiando...');
          this.logout();
          return;
        }
        
        const user = this.getUserFromToken(token);
        if (user && user.rol) {
          this.currentUserSubject.next(user);
          console.log('Usuario inicializado desde token:', user);
        } else {
          console.warn('No se pudo recuperar el usuario del token');
          this.currentUserSubject.next(null);
        }
      } catch (error) {
        console.error('Error al inicializar usuario desde token:', error);
        this.currentUserSubject.next(null);
      }
    } else {
      this.currentUserSubject.next(null);
    }
  }

  ensureUserInitialized(): boolean {
    const token = this.getToken();
    if (token && this.isAuthenticated()) {
      if (!this.currentUserSubject.value) {
        console.log('Inicializando usuario desde token...');
        this.initializeUser();
        const user = this.currentUserSubject.value;
        if (!user) {
          const userFromToken = this.getUserFromToken(token);
          if (userFromToken && userFromToken.rol) {
            this.currentUserSubject.next(userFromToken);
            console.log('Usuario inicializado directamente:', userFromToken);
            return true;
          }
          return false;
        }
        return true;
      } else {
        console.log('Usuario ya está inicializado:', this.currentUserSubject.value);
        return true;
      }
    } else {
      console.warn('No hay token válido para inicializar usuario');
      return false;
    }
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, loginRequest)
      .pipe(
        tap({
          next: (response) => {
            if (response.success && response.token) {
              this.setToken(response.token);
              const user: User = {
                id: response.userId,
                username: response.username,
                rol: response.rol
              };
              this.currentUserSubject.next(user);
              console.log('Usuario autenticado:', user);
            }
          },
          error: (error) => {
            console.error('Error en login:', error);
          }
        })
      );
  }

  logout(): void {
    sessionStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    try {
      const payload = this.decodeToken(token);
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  private setToken(token: string): void {
    sessionStorage.setItem('token', token);
  }

  getCurrentUser(): User | null {
    let user = this.currentUserSubject.value;
    if (!user) {
      const token = this.getToken();
      if (token && this.isAuthenticated()) {
        try {
          user = this.getUserFromToken(token);
          if (user && user.rol) {
            this.currentUserSubject.next(user);
            console.log('Usuario recuperado del token:', user);
          } else {
            console.warn('Usuario del token no tiene rol válido');
          }
        } catch (error) {
          console.error('Error al recuperar usuario del token:', error);
        }
      }
    }
    return user;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user ? user.rol === role : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.getCurrentUser();
    if (!user || !user.rol) {
      const token = this.getToken();
      if (token && this.isAuthenticated()) {
        const userFromToken = this.getUserFromToken(token);
        if (userFromToken && userFromToken.rol) {
          this.currentUserSubject.next(userFromToken);
          return roles.includes(userFromToken.rol);
        }
      }
      return false;
    }
    return roles.includes(user.rol);
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isBarbero(): boolean {
    return this.hasRole('BARBERO');
  }

  private decodeToken(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  }

  private getUserFromToken(token: string): User | null {
    try {
      const payload = this.decodeToken(token);
      console.log('Payload del token decodificado:', payload);
      
      const user: User = {
        id: payload.userId || payload.id,
        username: payload.sub || payload.username,
        rol: payload.rol
      };
      
      if (!user.rol) {
        console.error('El token no contiene el campo "rol"');
        return null;
      }
      
      console.log('Usuario extraído del token:', user);
      return user;
    } catch (error) {
      console.error('Error al decodificar token:', error);
      return null;
    }
  }

  getAuthHeaders(): { [key: string]: string } {
    const token = this.getToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }
}

