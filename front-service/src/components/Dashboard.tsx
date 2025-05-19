import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {projectsApi} from "../services/projectMainApi.ts";
import {ProjectDTO} from "../types/ProjectDTO.ts";
import {useEffect, useState} from "react";

export default function Dashboard() {
  const navigate = useNavigate();
  const { token, setToken } = useAuth();

  const handleSignOut = () => {
    setToken(null);
    navigate('/login');
  };

  const useProjects = (): string[] => {
    const [projectNames, setProjectNames] = useState<string[]>([]);

    useEffect(() => {
      const fetchProjects = async () => {
        try {
          const projects: ProjectDTO[] = await projectsApi.getAllProjects();
          const names = projects.map(project => project.name);
          setProjectNames(names);
        } catch (error) {
          console.error('Error fetching projects:', error);
          setProjectNames([]);
        }
      };

      fetchProjects();
    }, []);

    return projectNames;
  };



  const projects = useProjects()

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white shadow sm:rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Project:
            </h3>
            <div className="mt-2 max-w-xl text-sm text-gray-500">
              <p>Your current projects are:</p>
              <ul>
                {projects.map((name, index) => (
                    <li key={index}>{name}</li>
                ))}
              </ul>
            </div>
            <div className="mt-5">
              <button
                  onClick={handleSignOut}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                Sign Out
              </button>
              
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}