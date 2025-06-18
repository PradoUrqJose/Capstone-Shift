describe('Gestión de Empresas - Login y prueba de empresa', () => {
  it('Debe iniciar sesión y luego agregar una nueva empresa', () => {
    // Paso 1: Ir a la página de login
    cy.visit('http://localhost:4200');

    // Paso 2: Completar credenciales
    cy.get('#username').type('admin');
    cy.get('#password').type('admin');

    // Paso 3: Enviar el formulario de login
    cy.get('button[type="submit"]').click();

    // Paso 4: Esperar que redireccione a /empresas (ajusta si tu app redirige a otra ruta primero)
    cy.visit('http://localhost:4200/empresas');

    // Esperar un poco si hay animaciones o carga de datos
    cy.wait(2000);

    // Paso 5: Agregar empresa (tu prueba anterior)
    cy.contains('Agregar').click();
    cy.wait(2000);
    cy.get('#nombre').type('Empresa Cypress Test 2');
    cy.wait(1000);
    cy.get('#ruc').type('12345678910');
    cy.wait(1000);
    cy.get('input[type="checkbox"][formControlName="habilitada"]').check({ force: true });
    cy.wait(1000);
    cy.get('[data-cy=submit-empresa]').click();
    cy.wait(3000);
    cy.contains('td', 'Empresa Cypress Test').should('exist');
  });
});
