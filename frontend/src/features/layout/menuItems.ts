import { LayoutDashboard, Tag, User, BookOpen, Users, Calendar, type LucideIcon, } from 'lucide-react';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

export interface MenuItem {
  path: string;
  label: string;
  icon: LucideIcon;
}

export const menuItems: MenuItem[] = [
  { path: APP_ROUTES.DASHBOARD, label: 'Dashboard', icon: LayoutDashboard },
  { path: APP_ROUTES.CATEGORIAS, label: 'Categorías', icon: Tag },
  { path: APP_ROUTES.AUTORES, label: 'Autores', icon: User },
  { path: APP_ROUTES.LIBROS, label: 'Libros', icon: BookOpen },
  { path: APP_ROUTES.USUARIOS, label: 'Usuarios', icon: Users },
  { path: APP_ROUTES.PRESTAMOS, label: 'Préstamos', icon: Calendar },
];
