import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from '@/features/layout/Layout';
import { DashboardPage } from '@/features/dashboard/DashboardPage';
import { CategoriasPage } from '@/features/categorias/CategoriasPage';
import { AutoresPage } from '@/features/autores/AutoresPage';
import { LibrosPage } from '@/features/libros/LibrosPage';
import { UsuariosPage } from '@/features/usuarios/UsuariosPage';
import { PrestamosPage } from '@/features/prestamos/PrestamosPage';
import { PrestamoNuevoPage } from '@/features/prestamos/PrestamoNuevoPage';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

export const AppRouter = () => {
  return (
    <Layout>
      <Routes>
        <Route path={APP_ROUTES.HOME} element={<DashboardPage />} />
        <Route path={APP_ROUTES.DASHBOARD} element={<DashboardPage />} />
        <Route path={APP_ROUTES.CATEGORIAS} element={<CategoriasPage />} />
        <Route path={APP_ROUTES.AUTORES} element={<AutoresPage />} />
        <Route path={APP_ROUTES.LIBROS} element={<LibrosPage />} />
        <Route path={APP_ROUTES.USUARIOS} element={<UsuariosPage />} />
        <Route path={APP_ROUTES.PRESTAMOS} element={<PrestamosPage />} />
        <Route path={APP_ROUTES.PRESTAMOS_NUEVO} element={<PrestamoNuevoPage />} />
        <Route path="*" element={<Navigate to={APP_ROUTES.HOME} replace />} />
      </Routes>
    </Layout>
  );
};
