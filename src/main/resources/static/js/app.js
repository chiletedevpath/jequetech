"use strict";
const cliente = document.querySelector("[data-client-select]");
const equipo = document.querySelector("[data-equipment-select]");
if (cliente && equipo) {
  const opciones = Array.from(equipo.options)
    .slice(1)
    .map((option) => option.cloneNode(true));
  const filtrar = () => {
    const actual = equipo.value;
    equipo.replaceChildren(new Option("Selecciona un equipo del cliente", ""));
    opciones
      .filter((option) => option.dataset.client === cliente.value)
      .forEach((option) => equipo.add(option.cloneNode(true)));
    equipo.value = Array.from(equipo.options).some(
      (option) => option.value === actual,
    )
      ? actual
      : "";
  };
  cliente.addEventListener("change", filtrar);
  filtrar();
}
const rol = document.querySelector("[data-role-select]");
const vinculo = document.querySelector("[data-technician-link]");
if (rol && vinculo) {
  const actualizar = () => {
    vinculo.hidden = rol.value !== "TECNICO";
    vinculo.querySelector("select").required = rol.value === "TECNICO";
  };
  rol.addEventListener("change", actualizar);
  actualizar();
}
