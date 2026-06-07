import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import LeadsPage from './pages/LeadsPage';
import LeadDetailPage from './pages/LeadDetailPage';
import LeadFormPage from './pages/LeadFormPage';
import TasksPage from './pages/TasksPage';
import KanbanPage from './pages/KanbanPage';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={<ProtectedRoute><Layout><DashboardPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/leads"
            element={<ProtectedRoute><Layout><LeadsPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/leads/new"
            element={<ProtectedRoute><Layout><LeadFormPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/leads/:id"
            element={<ProtectedRoute><Layout><LeadDetailPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/leads/:id/edit"
            element={<ProtectedRoute><Layout><LeadFormPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/tasks"
            element={<ProtectedRoute><Layout><TasksPage /></Layout></ProtectedRoute>}
          />
          <Route
            path="/kanban"
            element={<ProtectedRoute><Layout><KanbanPage /></Layout></ProtectedRoute>}
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route
            path="*"
            element={<ProtectedRoute><Layout><Navigate to="/dashboard" replace /></Layout></ProtectedRoute>}
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
